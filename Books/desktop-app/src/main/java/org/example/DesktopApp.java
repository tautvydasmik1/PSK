package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.api.ApiClient;
import org.example.model.Book;

import org.example.model.Comment;
import org.example.model.Message;
import org.example.model.User;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Desktop Application for Books Management System
 *
 * This is the frontend-only application that communicates with the web backend.
 * All business logic is handled by the web server.
 */
public class DesktopApp extends Application {

    private ApiClient apiClient;
    private User currentUser;
    private Stage primaryStage;
    private TabPane mainTabPane;
    private VBox loginPane;
    private VBox mainContent;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.apiClient = new ApiClient();

        primaryStage.setTitle("Books Exchange System");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        showLoginScreen();
        primaryStage.show();
    }

    private void showLoginScreen() {
        loginPane = new VBox(20);
        loginPane.setPadding(new Insets(20));
        loginPane.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label("Books Exchange System");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(300);
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));

        loginPane.getChildren().addAll(titleLabel, usernameField, passwordField, loginButton);

        Scene scene = new Scene(loginPane, 400, 300);
        primaryStage.setScene(scene);
    }

    private void handleLogin(String username, String password) {
        System.out.println("Login button clicked! Username: " + username);

        // Show loading indicator
        Button loginButton = (Button) loginPane.getChildren().get(3);
        loginButton.setText("Logging in...");
        loginButton.setDisable(true);

        // Call the real login API
        apiClient.login(username, password)
            .thenAccept(user -> {
                javafx.application.Platform.runLater(() -> {
                    currentUser = user;
                    System.out.println("Login successful for user: " + user.getFullName());
                    showMainApplication();
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    loginButton.setText("Login");
                    loginButton.setDisable(false);
                    showAlert("Login Failed", "Invalid username or password. Please try again.");
                });
                return null;
            });
    }

    private void showMainApplication() {
        mainContent = new VBox(10);
        mainContent.setPadding(new Insets(10));

        // Create menu bar
        MenuBar menuBar = createMenuBar();

        // Create tab pane for different sections
        mainTabPane = new TabPane();

        if (currentUser.isAdmin()) {
            // Admin users don't need My Books tab
            mainTabPane.getTabs().addAll(
                createBooksTab(),
                createCommentsTab(),
                createMessagesTab()
            );
        } else {
            // Regular users get all tabs including My Books
            mainTabPane.getTabs().addAll(
                createBooksTab(),
                createMyBooksTab(),
                createCommentsTab(),
                createMessagesTab()
            );
        }

        // Add tab selection listener for auto-refresh
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null) {
                if ("Browse Books".equals(newTab.getText())) {
                    // Always refresh Browse Books when selected
                    refreshBooksList();
                } else if ("My Books".equals(newTab.getText()) && !currentUser.isAdmin()) {
                    // Refresh My Books when selected (only for non-admin users)
                    refreshMyBooks();
                }
            }
        });

        if (currentUser.isAdmin()) {
            mainTabPane.getTabs().add(createAdminTab());
        }

        mainContent.getChildren().addAll(menuBar, mainTabPane);

        Scene scene = new Scene(mainContent);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);

        // Load initial data after login
        loadInitialData();
    }

    private void loadInitialData() {
        // Load available books (excluding current user's books)
        apiClient.getAvailableBooksExcludingCurrentUser()
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    // Update the Browse Books tab
                    for (Tab tab : mainTabPane.getTabs()) {
                        if ("Browse Books".equals(tab.getText())) {
                            updateBooksTable(books, tab);
                            break;
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load books: " + throwable.getMessage());
                });
                return null;
            });

        // Load user's related books (owned + borrowed + reserved)
        apiClient.getAllUserRelatedBooks(currentUser.getId())
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    // Update the My Books tab
                    for (Tab tab : mainTabPane.getTabs()) {
                        if ("My Books".equals(tab.getText())) {
                            updateBooksTable(books, tab);
                            break;
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load your books: " + throwable.getMessage());
                });
                return null;
            });
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> showLoginScreen());
        fileMenu.getItems().add(logoutItem);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private Tab createBooksTab() {
        Tab tab = new Tab("Browse Books");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        // Search controls - 5 required filters
        HBox searchBox = new HBox(10);
        TextField searchField = new TextField();
        searchField.setPromptText("Search books...");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("All Categories");
        categoryCombo.setValue("All Categories");

        // Load categories dynamically
        loadCategoriesIntoCombo(categoryCombo);

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("All Statuses", "AVAILABLE", "BORROWED", "RESERVED");
        statusCombo.setValue("All Statuses");

        TextField authorField = new TextField();
        authorField.setPromptText("Author");
        authorField.setPrefWidth(120);

        TextField yearFromField = new TextField();
        yearFromField.setPromptText("Year From");
        yearFromField.setPrefWidth(80);

        TextField yearToField = new TextField();
        yearToField.setPromptText("Year To");
        yearToField.setPrefWidth(80);

        Button searchButton = new Button("Search");
        Button clearButton = new Button("Clear");

        // Books table (needs to be declared before the buttons)
        TableView<Book> booksTable = new TableView<>();

                // Add search functionality
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim().isEmpty() ? null : searchField.getText().trim();
            String category = "All Categories".equals(categoryCombo.getValue()) ? null : categoryCombo.getValue();
            String status = "All Statuses".equals(statusCombo.getValue()) ? null : statusCombo.getValue();
            String author = authorField.getText().trim().isEmpty() ? null : authorField.getText().trim();

            Integer yearFrom = null;
            Integer yearTo = null;
            try {
                if (!yearFromField.getText().trim().isEmpty()) {
                    yearFrom = Integer.parseInt(yearFromField.getText().trim());
                }
                if (!yearToField.getText().trim().isEmpty()) {
                    yearTo = Integer.parseInt(yearToField.getText().trim());
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "Please enter valid years (e.g., 2000)");
                return;
            }

            searchBooks(query, category, status, author, yearFrom, yearTo, booksTable);
        });

                // Add clear functionality
        clearButton.setOnAction(e -> {
            searchField.clear();
            categoryCombo.setValue("All Categories");
            statusCombo.setValue("All Statuses");
            authorField.clear();
            yearFromField.clear();
            yearToField.clear();

            // Reload all available books (default view)
            loadBooksForBrowseTab(booksTable);
        });

        searchBox.getChildren().addAll(searchField, categoryCombo, statusCombo, authorField, yearFromField, yearToField, searchButton, clearButton);

        // Configure the books table
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));

        TableColumn<Book, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory().getDisplayName()));
        categoryCol.setPrefWidth(100);

        TableColumn<Book, String> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getPublicationYear() != null ? data.getValue().getPublicationYear().toString() : ""));
        yearCol.setPrefWidth(60);

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        TableColumn<Book, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDescription() != null ? data.getValue().getDescription() : ""));
        descriptionCol.setPrefWidth(200);

        TableColumn<Book, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerName()));

        booksTable.getColumns().addAll(titleCol, authorCol, categoryCol, yearCol, statusCol, descriptionCol, ownerCol);
        booksTable.setPrefHeight(400);

        // Action buttons (different for admin vs regular users)
        HBox actionBox = new HBox(10);

        if (currentUser.isAdmin()) {
            // Admin users only get Contact Owner (for administrative purposes)
            Button contactOwnerButton = new Button("Contact Owner");
            contactOwnerButton.setOnAction(e -> {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    showContactOwnerDialog(selectedBook);
                } else {
                    showAlert("Error", "Please select a book to contact the owner");
                }
            });
            actionBox.getChildren().add(contactOwnerButton);
        } else {
            // Regular users get full functionality
            Button reserveButton = new Button("Reserve");
            Button borrowButton = new Button("Borrow");
            Button contactOwnerButton = new Button("Contact Owner");

            // Add event handlers for borrow and reserve buttons
            borrowButton.setOnAction(e -> {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    borrowBook(selectedBook);
                } else {
                    showAlert("Error", "Please select a book to borrow");
                }
            });

            reserveButton.setOnAction(e -> {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    reserveBook(selectedBook);
                } else {
                    showAlert("Error", "Please select a book to reserve");
                }
            });

            contactOwnerButton.setOnAction(e -> {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    showContactOwnerDialog(selectedBook);
                } else {
                    showAlert("Error", "Please select a book to contact the owner");
                }
            });

            actionBox.getChildren().addAll(reserveButton, borrowButton, contactOwnerButton);
        }

        content.getChildren().addAll(searchBox, booksTable, actionBox);
        tab.setContent(content);

        return tab;
    }

    private Tab createMyBooksTab() {
        Tab tab = new Tab("My Books");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label titleLabel = new Label("My Books");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<Book> myBooksTable = new TableView<>();
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));

        TableColumn<Book, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory().getDisplayName()));
        categoryCol.setPrefWidth(100);

        TableColumn<Book, String> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getPublicationYear() != null ? data.getValue().getPublicationYear().toString() : ""));
        yearCol.setPrefWidth(60);

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        TableColumn<Book, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
            data.getValue().getDescription() != null ? data.getValue().getDescription() : ""));
        descriptionCol.setPrefWidth(200);

        TableColumn<Book, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerName()));

        myBooksTable.getColumns().addAll(titleCol, authorCol, categoryCol, yearCol, statusCol, descriptionCol, ownerCol);

        HBox actionBox = new HBox(10);
        Button addBookButton = new Button("Add New Book");
        Button editBookButton = new Button("Edit Book");
        Button deleteBookButton = new Button("Delete Book");
        Button returnBookButton = new Button("Return Book");

        // Add event handler for Add New Book button
        addBookButton.setOnAction(e -> showAddBookDialog());

        // Add event handler for Edit Book button
        editBookButton.setOnAction(e -> {
            Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                showEditBookDialog(selectedBook);
            } else {
                showAlert("Error", "Please select a book to edit");
            }
        });

        // Add event handler for Delete Book button
        deleteBookButton.setOnAction(e -> {
            Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                deleteUserBook(selectedBook);
            } else {
                showAlert("Error", "Please select a book to delete");
            }
        });

        // Add event handler for Return Book button
        returnBookButton.setOnAction(e -> {
            Book selectedBook = myBooksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                returnBook(selectedBook);
            } else {
                showAlert("Error", "Please select a book to return");
            }
        });

        actionBox.getChildren().addAll(addBookButton, editBookButton, deleteBookButton, returnBookButton);

        content.getChildren().addAll(titleLabel, myBooksTable, actionBox);
        tab.setContent(content);

        return tab;
    }

    private Tab createCommentsTab() {
        Tab tab = new Tab("Comments");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label titleLabel = new Label("Recent Comments");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Book selection for viewing comments
        HBox bookSelectionBox = new HBox(10);
        Label bookLabel = new Label("Select Book:");
        ComboBox<Book> bookCombo = new ComboBox<>();
        bookCombo.setPromptText("Choose a book to view comments");
        bookCombo.setMaxWidth(300);

        Button refreshBooksBtn = new Button("Refresh Books");
        refreshBooksBtn.setOnAction(e -> loadBooksForComments(bookCombo));

        bookSelectionBox.getChildren().addAll(bookLabel, bookCombo, refreshBooksBtn);

        // Comments display area
        VBox commentsArea = new VBox(10);
        commentsArea.setStyle("-fx-border-color: #ccc; -fx-border-width: 1px; -fx-padding: 10px;");
        commentsArea.setPrefHeight(400);

        // Add comment section
        VBox addCommentBox = new VBox(10);
        Label addCommentLabel = new Label("Add Comment:");
        TextArea commentTextArea = new TextArea();
        commentTextArea.setPromptText("Write your comment here...");
        commentTextArea.setPrefRowCount(3);

        Button addCommentBtn = new Button("Add Comment");
        addCommentBtn.setOnAction(e -> {
            Book selectedBook = bookCombo.getValue();
            String commentText = commentTextArea.getText().trim();
            if (selectedBook != null && !commentText.isEmpty()) {
                addComment(selectedBook.getId(), commentText, commentsArea, commentTextArea);
            } else {
                showAlert("Error", "Please select a book and enter a comment.");
            }
        });

        addCommentBox.getChildren().addAll(addCommentLabel, commentTextArea, addCommentBtn);

        // Load comments when book is selected
        bookCombo.setOnAction(e -> {
            Book selectedBook = bookCombo.getValue();
            if (selectedBook != null) {
                loadCommentsForBook(selectedBook.getId(), commentsArea);
            }
        });

        content.getChildren().addAll(titleLabel, bookSelectionBox, commentsArea, addCommentBox);
        tab.setContent(content);

        // Load books initially
        loadBooksForComments(bookCombo);

        return tab;
    }



    private Tab createMessagesTab() {
        Tab tab = new Tab("Messages");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label titleLabel = new Label("Messages");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Messages display area
        VBox messagesArea = new VBox(10);
        messagesArea.setStyle("-fx-border-color: #ddd; -fx-border-width: 1px; -fx-padding: 15px;");
        messagesArea.setPrefHeight(500);

        // Refresh button
        Button refreshButton = new Button("Refresh Messages");
        refreshButton.setOnAction(e -> loadUserMessages(messagesArea));

        content.getChildren().addAll(titleLabel, refreshButton, messagesArea);
        tab.setContent(content);

        // Load messages initially
        loadUserMessages(messagesArea);

        return tab;
    }

    private Tab createAdminTab() {
        Tab tab = new Tab("Admin Panel");
        tab.setClosable(false);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label titleLabel = new Label("Administrator Panel");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                        HBox adminButtons = new HBox(10);
        Button manageUsersButton = new Button("Manage Users");
        Button manageBooksButton = new Button("Manage Books");
        Button userActivityButton = new Button("User Activity");

        // Add event handlers for admin buttons
        manageUsersButton.setOnAction(e -> showManageUsersDialog());
        manageBooksButton.setOnAction(e -> showManageBooksDialog());
        userActivityButton.setOnAction(e -> showUserActivityDialog());

        adminButtons.getChildren().addAll(manageUsersButton, manageBooksButton, userActivityButton);

        content.getChildren().addAll(titleLabel, adminButtons);
        tab.setContent(content);

        return tab;
    }

    private void showAddBookDialog() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Add New Book");

        TextField titleField = new TextField();
        titleField.setPromptText("Book Title");

        TextField authorField = new TextField();
        authorField.setPromptText("Author");

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Select Category");

        // Load categories dynamically
        loadCategoriesIntoCombo(categoryCombo);

        TextField yearField = new TextField();
        yearField.setPromptText("Publication Year (e.g., 2023)");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(3);

        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String category = categoryCombo.getValue();
                String description = descriptionArea.getText().trim();
                String yearText = yearField.getText().trim();

                if (title.isEmpty() || author.isEmpty() || category == null || category.isEmpty()) {
                    showAlert("Error", "Please fill in all required fields (Title, Author, Category)");
                    return;
                }

                Integer publicationYear = null;
                if (!yearText.isEmpty()) {
                    try {
                        publicationYear = Integer.parseInt(yearText);
                        if (publicationYear < 1000 || publicationYear > 2030) {
                            showAlert("Error", "Please enter a valid publication year (1000-2030)");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        showAlert("Error", "Please enter a valid publication year (numbers only)");
                        return;
                    }
                }

                // Create book with all fields including publication year
                apiClient.addBook(title, author, category, description, publicationYear, currentUser.getId())
                    .thenAccept(book -> {
                        javafx.application.Platform.runLater(() -> {
                            if (book != null) {
                                showAlert("Success", "Book '" + title + "' has been added successfully!");
                                dialog.close();
                                refreshMyBooks();
                            } else {
                                showAlert("Error", "Failed to add book");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Error adding book: " + throwable.getMessage());
                        });
                        return null;
                    });

            } catch (Exception ex) {
                showAlert("Error", "Error adding book: " + ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        VBox form = new VBox(10);
        form.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Author:"), authorField,
            new Label("Category:"), categoryCombo,
            new Label("Publication Year (optional):"), yearField,
            new Label("Description:"), descriptionArea
        );

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(saveButton, cancelButton);

        VBox content = new VBox(10);
        content.getChildren().addAll(form, buttons);
        content.setPadding(new Insets(10));

        dialog.setScene(new Scene(content));
        dialog.showAndWait();
    }

    private void showEditBookDialog(Book book) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Book - " + book.getTitle());

        TextField titleField = new TextField();
        titleField.setPromptText("Book Title");
        titleField.setText(book.getTitle());

        TextField authorField = new TextField();
        authorField.setPromptText("Author");
        authorField.setText(book.getAuthor());

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setValue(book.getCategory().getDisplayName());

        // Load categories dynamically
        loadCategoriesIntoCombo(categoryCombo);

        TextField yearField = new TextField();
        yearField.setPromptText("Publication Year (e.g., 2023)");
        yearField.setText(book.getPublicationYear() != null ? book.getPublicationYear().toString() : "");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setText(book.getDescription() != null ? book.getDescription() : "");
        descriptionArea.setPrefRowCount(3);

        Button saveButton = new Button("Save Changes");
        Button cancelButton = new Button("Cancel");

        saveButton.setOnAction(e -> {
            try {
                String title = titleField.getText().trim();
                String author = authorField.getText().trim();
                String category = categoryCombo.getValue();
                String description = descriptionArea.getText().trim();
                String yearText = yearField.getText().trim();

                if (title.isEmpty() || author.isEmpty() || category == null || category.isEmpty()) {
                    showAlert("Error", "Please fill in all required fields (Title, Author, Category)");
                    return;
                }

                Integer publicationYear = null;
                if (!yearText.isEmpty()) {
                    try {
                        publicationYear = Integer.parseInt(yearText);
                        if (publicationYear < 1000 || publicationYear > 2030) {
                            showAlert("Error", "Please enter a valid publication year (1000-2030)");
                            return;
                        }
                    } catch (NumberFormatException ex) {
                        showAlert("Error", "Please enter a valid publication year (numbers only)");
                        return;
                    }
                }

                // Update book with edited fields including publication year
                apiClient.updateBook(book.getId(), title, author, category, description, publicationYear)
                    .thenAccept(updatedBook -> {
                        javafx.application.Platform.runLater(() -> {
                            if (updatedBook != null) {
                                showAlert("Success", "Book '" + title + "' has been updated successfully!");
                                dialog.close();
                                // Refresh both tabs to show updated information
                                refreshMyBooks();
                                refreshBooksList();
                            } else {
                                showAlert("Error", "Failed to update book");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Error updating book: " + throwable.getMessage());
                        });
                        return null;
                    });

            } catch (Exception ex) {
                showAlert("Error", "Error updating book: " + ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        VBox form = new VBox(10);
        form.getChildren().addAll(
            new Label("Title:"), titleField,
            new Label("Author:"), authorField,
            new Label("Category:"), categoryCombo,
            new Label("Publication Year (optional):"), yearField,
            new Label("Description:"), descriptionArea
        );

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(saveButton, cancelButton);

        VBox content = new VBox(10);
        content.getChildren().addAll(form, buttons);
        content.setPadding(new Insets(10));

        dialog.setScene(new Scene(content));
        dialog.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshMyBooks() {
        if (currentUser == null) {
            showAlert("Error", "No user logged in");
            return;
        }

        // Reload user-related books from the API and update the table
        apiClient.getAllUserRelatedBooks(currentUser.getId())
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    // Find the My Books tab and update its content
                    for (Tab tab : mainTabPane.getTabs()) {
                        if ("My Books".equals(tab.getText())) {
                            // Update the table with new data
                            updateBooksTable(books, tab);
                            break;
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load your books: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void updateBooksTable(List<Book> books, Tab tab) {
        // Get the table from the tab content
        VBox content = (VBox) tab.getContent();
        for (javafx.scene.Node node : content.getChildren()) {
            if (node instanceof TableView) {
                @SuppressWarnings("unchecked")
                TableView<Book> table = (TableView<Book>) node;

                // Clear existing items and add new ones
                table.getItems().clear();
                table.getItems().addAll(books);
                break;
            }
        }
    }

    private void borrowBook(Book book) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in");
            return;
        }

        apiClient.borrowBook(book.getId(), currentUser.getId())
            .thenAccept(success -> {
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        showAlert("Success", "Book '" + book.getTitle() + "' has been borrowed successfully!");
                        // Refresh both Browse Books and My Books to show updated status
                        refreshBooksList();
                        refreshMyBooks();
                    } else {
                        showAlert("Error", "Failed to borrow book: " + book.getTitle());
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to borrow book: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void reserveBook(Book book) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in");
            return;
        }

        apiClient.reserveBook(book.getId(), currentUser.getId())
            .thenAccept(success -> {
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        showAlert("Success", "Book '" + book.getTitle() + "' has been reserved successfully!");
                        // Refresh both Browse Books and My Books to show updated status
                        refreshBooksList();
                        refreshMyBooks();
                    } else {
                        showAlert("Error", "Failed to reserve book: " + book.getTitle());
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to reserve book: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void returnBook(Book book) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in");
            return;
        }

        apiClient.returnBook(book.getId(), currentUser.getId())
            .thenAccept(success -> {
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        showAlert("Success", "Book '" + book.getTitle() + "' has been returned successfully!");
                        // Refresh both My Books and Browse Books to show updated status
                        refreshMyBooks();
                        refreshBooksList();
                    } else {
                        showAlert("Error", "Failed to return book: " + book.getTitle());
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to return book: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void deleteUserBook(Book book) {
        if (currentUser == null) {
            showAlert("Error", "No user logged in");
            return;
        }

        // Check if the book is owned by the current user (not borrowed/reserved)
        if (!book.getOwnerId().equals(currentUser.getId())) {
            showAlert("Error", "You can only delete books that you own. You cannot delete borrowed or reserved books.");
            return;
        }

        // Show confirmation dialog
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete Book");
        confirmation.setContentText("Are you sure you want to permanently delete \"" + book.getTitle() + "\"?\n\nThis action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiClient.deleteUserBook(book.getId())
                    .thenAccept(success -> {
                        javafx.application.Platform.runLater(() -> {
                            if (success) {
                                showAlert("Success", "Book '" + book.getTitle() + "' has been deleted successfully!");
                                // Refresh both My Books and Browse Books to update the listings
                                refreshMyBooks();
                                refreshBooksList();
                            } else {
                                showAlert("Error", "Failed to delete book: " + book.getTitle());
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Failed to delete book: " + throwable.getMessage());
                        });
                        return null;
                    });
            }
        });
    }

    private void refreshBooksList() {
        // Refresh the books table in the Browse Books tab with current filters
        for (Tab tab : mainTabPane.getTabs()) {
            if ("Browse Books".equals(tab.getText())) {
                // Get current filter values from the tab
                VBox content = (VBox) tab.getContent();
                String query = "";
                String category = "All Categories";
                String status = "All Statuses";
                String author = "";
                Integer yearFrom = null;
                Integer yearTo = null;

                // Find filter controls and get their current values
                for (javafx.scene.Node node : content.getChildren()) {
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        for (javafx.scene.Node child : hbox.getChildren()) {
                            if (child instanceof TextField) {
                                TextField field = (TextField) child;
                                if (field.getPromptText() != null) {
                                    if (field.getPromptText().contains("Search")) {
                                        query = field.getText();
                                    } else if (field.getPromptText().contains("Author")) {
                                        author = field.getText();
                                    } else if (field.getPromptText().contains("Year From")) {
                                        try {
                                            String text = field.getText().trim();
                                            if (!text.isEmpty()) {
                                                yearFrom = Integer.parseInt(text);
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    } else if (field.getPromptText().contains("Year To")) {
                                        try {
                                            String text = field.getText().trim();
                                            if (!text.isEmpty()) {
                                                yearTo = Integer.parseInt(text);
                                            }
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }
                            } else if (child instanceof ComboBox) {
                                @SuppressWarnings("unchecked")
                                ComboBox<String> combo = (ComboBox<String>) child;
                                if (combo.getItems().contains("All Categories")) {
                                    category = combo.getValue();
                                } else if (combo.getItems().contains("All Statuses")) {
                                    status = combo.getValue();
                                }
                            }
                        }
                    }
                }

                // Find the table and refresh it with current filters
                for (javafx.scene.Node node : content.getChildren()) {
                    if (node instanceof TableView) {
                        @SuppressWarnings("unchecked")
                        TableView<Book> booksTable = (TableView<Book>) node;
                        searchBooks(query, category, status, author, yearFrom, yearTo, booksTable);
                        break;
                    }
                }
                break;
            }
        }
    }

    private void searchBooks(String query, String category, String status, String author, Integer yearFrom, Integer yearTo, TableView<Book> booksTable) {
        // Convert "All Categories" and "All Statuses" to null for API call
        String categoryParam = ("All Categories".equals(category)) ? null : category;
        String statusParam = ("All Statuses".equals(status)) ? null : status;

        // Use unified search with all filters - server handles everything
        apiClient.searchBooksUnified(query, categoryParam, author, statusParam, yearFrom, yearTo, true)
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    booksTable.getItems().clear();
                    booksTable.getItems().addAll(books);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to search books: " + throwable.getMessage());
                });
                return null;
            });
    }





    private void loadBooksForBrowseTab(TableView<Book> booksTable) {
        // Load all available books excluding current user
        apiClient.getAvailableBooksExcludingCurrentUser()
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    booksTable.getItems().clear();
                    booksTable.getItems().addAll(books);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load books: " + throwable.getMessage());
                });
                return null;
            });
    }

    // Comment functionality methods
    private void loadBooksForComments(ComboBox<Book> bookCombo) {
        apiClient.getAllBooks()
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    bookCombo.getItems().clear();
                    bookCombo.getItems().addAll(books);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load books: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void loadCommentsForBook(String bookId, VBox commentsArea) {
        commentsArea.getChildren().clear();
        commentsArea.getChildren().add(new Label("Loading comments..."));

        apiClient.getNestedCommentsByBookId(bookId)
            .thenAccept(comments -> {
                javafx.application.Platform.runLater(() -> {
                    commentsArea.getChildren().clear();
                    if (comments.isEmpty()) {
                        commentsArea.getChildren().add(new Label("No comments yet. Be the first to comment!"));
                    } else {
                        displayComments(comments, commentsArea, bookId, 0);
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    commentsArea.getChildren().clear();
                    commentsArea.getChildren().add(new Label("Error loading comments: " + throwable.getMessage()));
                });
                return null;
            });
    }

    private void displayComments(List<Comment> comments, VBox container, String bookId, int depth) {
        for (Comment comment : comments) {
            VBox commentBox = createCommentBox(comment, bookId, depth);
            container.getChildren().add(commentBox);

            // Display replies recursively
            if (comment.hasReplies()) {
                VBox repliesContainer = new VBox(5);
                repliesContainer.setStyle("-fx-padding: 10px 0px 0px 20px; -fx-border-left: 2px solid #e0e0e0;");
                displayComments(comment.getReplies(), repliesContainer, bookId, depth + 1);
                container.getChildren().add(repliesContainer);
            }
        }
    }

    private VBox createCommentBox(Comment comment, String bookId, int depth) {
        VBox commentBox = new VBox(5);
        commentBox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Comment header
        HBox headerBox = new HBox(10);
        Label authorLabel = new Label(comment.getAuthorName());
        authorLabel.setStyle("-fx-font-weight: bold;");
        Label dateLabel = new Label(comment.getFormattedCreatedAt());
        dateLabel.setStyle("-fx-text-fill: #666;");
        headerBox.getChildren().addAll(authorLabel, dateLabel);

        // Comment content
        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-padding: 5px 0px;");

        // Action buttons
        HBox actionBox = new HBox(10);
        Button replyBtn = new Button("Reply");
        replyBtn.setOnAction(e -> showReplyDialog(comment, bookId, commentBox));

        // Only show edit/delete buttons for the comment author
        if (comment.getAuthorId().equals(currentUser.getId())) {
            Button editBtn = new Button("Edit");
            editBtn.setOnAction(e -> showEditDialog(comment, commentBox));

            Button deleteBtn = new Button("Delete");
            deleteBtn.setOnAction(e -> deleteComment(comment, commentBox));

            actionBox.getChildren().addAll(replyBtn, editBtn, deleteBtn);
        } else {
            actionBox.getChildren().add(replyBtn);
        }

        commentBox.getChildren().addAll(headerBox, contentLabel, actionBox);
        return commentBox;
    }

    private void addComment(String bookId, String content, VBox commentsArea, TextArea commentTextArea) {
        apiClient.createComment(bookId, content)
            .thenAccept(comment -> {
                javafx.application.Platform.runLater(() -> {
                    commentTextArea.clear();
                    loadCommentsForBook(bookId, commentsArea);
                    showAlert("Success", "Comment added successfully!");
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to add comment: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void showReplyDialog(Comment parentComment, String bookId, VBox commentBox) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Reply to Comment");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        Label replyToLabel = new Label("Replying to: " + parentComment.getAuthorName());
        replyToLabel.setStyle("-fx-font-weight: bold;");

        TextArea replyTextArea = new TextArea();
        replyTextArea.setPromptText("Write your reply...");
        replyTextArea.setPrefRowCount(4);

        HBox buttonBox = new HBox(10);
        Button submitBtn = new Button("Submit Reply");
        Button cancelBtn = new Button("Cancel");

        submitBtn.setOnAction(e -> {
            String replyText = replyTextArea.getText().trim();
            if (!replyText.isEmpty()) {
                apiClient.createReply(parentComment.getId(), replyText)
                    .thenAccept(reply -> {
                        javafx.application.Platform.runLater(() -> {
                            try {
                                dialog.close();

                                // Check if reply was created successfully
                                if (reply != null) {
                                    // Refresh the entire comments area
                                    for (Tab tab : mainTabPane.getTabs()) {
                                        if ("Comments".equals(tab.getText())) {
                                            VBox tabContent = (VBox) tab.getContent();
                                            if (tabContent.getChildren().size() > 2) {
                                                VBox commentsArea = (VBox) tabContent.getChildren().get(2);
                                                loadCommentsForBook(bookId, commentsArea);
                                            }
                                            break;
                                        }
                                    }
                                    showAlert("Success", "Reply added successfully!");
                                } else {
                                    showAlert("Warning", "Reply may have been added, but confirmation was not received.");
                                }
                            } catch (Exception ex) {
                                System.err.println("Error refreshing comments after reply: " + ex.getMessage());
                                ex.printStackTrace();
                                showAlert("Success", "Reply added successfully!");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Failed to add reply: " + throwable.getMessage());
                        });
                        return null;
                    });
            } else {
                showAlert("Error", "Please enter a reply.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());
        buttonBox.getChildren().addAll(submitBtn, cancelBtn);

        content.getChildren().addAll(replyToLabel, replyTextArea, buttonBox);
        dialog.setScene(new Scene(content, 400, 300));
        dialog.showAndWait();
    }

    private void showEditDialog(Comment comment, VBox commentBox) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Edit Comment");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextArea editTextArea = new TextArea(comment.getContent());
        editTextArea.setPrefRowCount(4);

        HBox buttonBox = new HBox(10);
        Button saveBtn = new Button("Save");
        Button cancelBtn = new Button("Cancel");

        saveBtn.setOnAction(e -> {
            String newContent = editTextArea.getText().trim();
            if (!newContent.isEmpty()) {
                apiClient.updateComment(comment.getId(), newContent)
                    .thenAccept(updatedComment -> {
                        javafx.application.Platform.runLater(() -> {
                            dialog.close();
                            // Update the comment display
                            Label contentLabel = (Label) commentBox.getChildren().get(1);
                            contentLabel.setText(updatedComment.getContent());
                            showAlert("Success", "Comment updated successfully!");
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Failed to update comment: " + throwable.getMessage());
                        });
                        return null;
                    });
            } else {
                showAlert("Error", "Comment cannot be empty.");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());
        buttonBox.getChildren().addAll(saveBtn, cancelBtn);

        content.getChildren().addAll(editTextArea, buttonBox);
        dialog.setScene(new Scene(content, 400, 300));
        dialog.showAndWait();
    }

    private void deleteComment(Comment comment, VBox commentBox) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Comment");
        alert.setHeaderText("Are you sure you want to delete this comment?");
        alert.setContentText("This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiClient.deleteComment(comment.getId())
                    .thenAccept(success -> {
                        javafx.application.Platform.runLater(() -> {
                            if (success) {
                                // Remove the comment from the UI
                                VBox parent = (VBox) commentBox.getParent();
                                parent.getChildren().remove(commentBox);
                                showAlert("Success", "Comment deleted successfully!");
                            } else {
                                showAlert("Error", "Failed to delete comment.");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Failed to delete comment: " + throwable.getMessage());
                        });
                        return null;
                    });
            }
        });
    }























    private void loadUserMessages(VBox messagesArea) {
        messagesArea.getChildren().clear();
        messagesArea.getChildren().add(new Label("Loading messages..."));

        apiClient.getUserMessages(currentUser.getId())
            .thenAccept(messages -> {
                javafx.application.Platform.runLater(() -> {
                    messagesArea.getChildren().clear();

                    if (messages.isEmpty()) {
                        messagesArea.getChildren().add(new Label("No messages yet"));
                    } else {
                        for (Message message : messages) {
                            VBox messageBox = createMessageBox(message);
                            messagesArea.getChildren().add(messageBox);
                        }
                    }
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    messagesArea.getChildren().clear();
                    messagesArea.getChildren().add(new Label("Error loading messages: " + throwable.getMessage()));
                });
                return null;
            });
    }

        private void showContactOwnerDialog(Book book) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Contact Book Owner");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Contact Owner: " + book.getOwnerName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label bookLabel = new Label("Book: " + book.getTitle() + " by " + book.getAuthor());
        bookLabel.setStyle("-fx-font-size: 14px;");

        TextArea messageArea = new TextArea();
        messageArea.setPromptText("Enter your message to the owner...");
        messageArea.setPrefRowCount(4);
        messageArea.setWrapText(true);

        HBox buttonBox = new HBox(10);
        Button sendButton = new Button("Send Message");
        Button cancelButton = new Button("Cancel");

        sendButton.setOnAction(e -> {
            String message = messageArea.getText().trim();
            if (message.isEmpty()) {
                showAlert("Error", "Please enter a message");
                return;
            }

            // Send message to owner
            apiClient.sendMessage(currentUser.getId(), book.getId(), message, null, book.getOwnerId())
                .thenAccept(sentMessage -> {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Success", "Message sent to " + book.getOwnerName());
                        dialog.close();
                    });
                })
                .exceptionally(throwable -> {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Error", "Failed to send message: " + throwable.getMessage());
                    });
                    return null;
                });
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(sendButton, cancelButton);

        content.getChildren().addAll(titleLabel, bookLabel, messageArea, buttonBox);

        Scene scene = new Scene(content, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void showReplyDialog(Message originalMessage) {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Reply to Message");

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label titleLabel = new Label("Reply to: " + originalMessage.getSenderName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label bookLabel = new Label("Book: " + originalMessage.getBookTitle());
        bookLabel.setStyle("-fx-font-size: 14px;");

        // Show original message content for context
        Label originalLabel = new Label("Original message:");
        originalLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #666;");

        Label originalContent = new Label(originalMessage.getContent());
        originalContent.setWrapText(true);
        originalContent.setStyle("-fx-font-size: 12px; -fx-text-fill: #666; -fx-padding: 5px; -fx-background-color: #f5f5f5; -fx-border-radius: 3px;");
        originalContent.setMaxWidth(350);

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Enter your reply...");
        replyArea.setPrefRowCount(4);
        replyArea.setWrapText(true);

        HBox buttonBox = new HBox(10);
        Button sendButton = new Button("Send Reply");
        Button cancelButton = new Button("Cancel");

        sendButton.setOnAction(e -> {
            String reply = replyArea.getText().trim();
            if (reply.isEmpty()) {
                showAlert("Error", "Please enter a reply");
                return;
            }

            // Send reply with original message as parent
            apiClient.sendMessage(currentUser.getId(), originalMessage.getBookId(), reply, originalMessage.getId(), originalMessage.getSenderId())
                .thenAccept(sentMessage -> {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Success", "Reply sent to " + originalMessage.getSenderName());
                        dialog.close();
                        // Refresh messages to show the new reply
                        Tab messagesTab = null;
                        for (Tab tab : mainTabPane.getTabs()) {
                            if ("Messages".equals(tab.getText())) {
                                messagesTab = tab;
                                break;
                            }
                        }
                        if (messagesTab != null) {
                            VBox messagesArea = (VBox) ((VBox) messagesTab.getContent()).getChildren().get(2);
                            loadUserMessages(messagesArea);
                        }
                    });
                })
                .exceptionally(throwable -> {
                    javafx.application.Platform.runLater(() -> {
                        showAlert("Error", "Failed to send reply: " + throwable.getMessage());
                    });
                    return null;
                });
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(sendButton, cancelButton);

        content.getChildren().addAll(titleLabel, bookLabel, originalLabel, originalContent, replyArea, buttonBox);

        Scene scene = new Scene(content, 400, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private VBox createMessageBox(Message message) {
        VBox messageBox = new VBox(5);
        messageBox.setStyle("-fx-padding: 10px; -fx-border-color: #ddd; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Message header with unread indicator
        HBox headerBox = new HBox(10);
        Label senderLabel = new Label(message.getSenderName());
        senderLabel.setStyle("-fx-font-weight: bold;");

        Label dateLabel = new Label(message.getFormattedCreatedAt());
        dateLabel.setStyle("-fx-text-fill: #666;");

        // Handle deleted messages - different styling
        if (message.isDeleted()) {
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #999; -fx-font-style: italic;");
            messageBox.setStyle("-fx-padding: 10px; -fx-border-color: #ccc; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-background-color: #f9f9f9;");
        }
        // Unread indicator - different font weight for unread messages
        else if (!message.isRead()) {
            senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0066cc;");
            messageBox.setStyle("-fx-padding: 10px; -fx-border-color: #0066cc; -fx-border-width: 2px; -fx-border-radius: 5px; -fx-background-color: #f0f8ff;");
        }

        headerBox.getChildren().addAll(senderLabel, dateLabel);

        // Message content
        Label contentLabel = new Label(message.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-padding: 5px 0px;");

        // Show deleted indicator for deleted messages
        if (message.isDeleted()) {
            contentLabel.setStyle("-fx-padding: 5px 0px; -fx-text-fill: #999; -fx-font-style: italic;");
            Label deletedLabel = new Label("(Message deleted)");
            deletedLabel.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-font-size: 10px;");
            messageBox.getChildren().addAll(headerBox, contentLabel, deletedLabel);
        } else {
            messageBox.getChildren().addAll(headerBox, contentLabel);

            // Add action buttons for non-deleted messages
            HBox actionBox = new HBox(10);
            actionBox.setStyle("-fx-padding: 5px 0px;");

            // Only show delete button if current user is the sender
            if (currentUser != null && message.getSenderId().equals(currentUser.getId())) {
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 10px;");
                deleteButton.setOnAction(e -> deleteMessage(message, messageBox));
                actionBox.getChildren().add(deleteButton);
            }

            // Show reply button if current user is NOT the sender (so they can reply)
            if (currentUser != null && !message.getSenderId().equals(currentUser.getId())) {
                Button replyButton = new Button("Reply");
                replyButton.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 10px;");
                replyButton.setOnAction(e -> showReplyDialog(message));
                actionBox.getChildren().add(replyButton);
            }

            if (!actionBox.getChildren().isEmpty()) {
                messageBox.getChildren().add(actionBox);
            }
        }

        // Book info
        Label bookLabel = new Label("Book: " + message.getBookTitle());
        bookLabel.setStyle("-fx-text-fill: #666; -fx-font-style: italic;");
        messageBox.getChildren().add(bookLabel);

        return messageBox;
    }

    private void deleteMessage(Message message, VBox messageBox) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Message");
        confirmation.setHeaderText("Are you sure you want to delete this message?");
        confirmation.setContentText("The message will still be visible but marked as deleted. This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiClient.deleteMessage(message.getId())
                    .thenAccept(success -> {
                        javafx.application.Platform.runLater(() -> {
                            if (success) {
                                showAlert("Success", "Message deleted successfully!");
                                // Update message display to show as deleted
                                message.setDeleted(true);
                                // Refresh the message display - recreate the message box
                                VBox parent = (VBox) messageBox.getParent();
                                if (parent != null) {
                                    int index = parent.getChildren().indexOf(messageBox);
                                    parent.getChildren().set(index, createMessageBox(message));
                                }
                            } else {
                                showAlert("Error", "Failed to delete message.");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Failed to delete message: " + throwable.getMessage());
                        });
                        return null;
                    });
            }
        });
    }

    // Admin functionality methods
    private void showManageUsersDialog() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Manage Users");
        dialog.setWidth(800);
        dialog.setHeight(600);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("User Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Users table
        TableView<User> usersTable = new TableView<>();
        TableColumn<User, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        idCol.setPrefWidth(100);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getFullName()));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));

        TableColumn<User, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserType().toString()));

        usersTable.getColumns().addAll(idCol, usernameCol, nameCol, emailCol, typeCol);

        // Action buttons
        HBox actionBox = new HBox(10);
        Button refreshButton = new Button("Refresh");
        Button deleteButton = new Button("Delete User");
        Button closeButton = new Button("Close");

        refreshButton.setOnAction(e -> loadAllUsers(usersTable));
        deleteButton.setOnAction(e -> {
            User selectedUser = usersTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                deleteUser(selectedUser, usersTable);
            } else {
                showAlert("Error", "Please select a user to delete");
            }
        });
        closeButton.setOnAction(e -> dialog.close());

        actionBox.getChildren().addAll(refreshButton, deleteButton, closeButton);

        content.getChildren().addAll(titleLabel, usersTable, actionBox);
        dialog.setScene(new Scene(content));

        // Load initial data
        loadAllUsers(usersTable);

        dialog.show();
    }

    private void showManageBooksDialog() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Manage Books");
        dialog.setWidth(900);
        dialog.setHeight(600);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("Book Management");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Books table (same as Browse Books but with admin actions)
        TableView<Book> booksTable = new TableView<>();
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));

        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));

        TableColumn<Book, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory().getDisplayName()));

        TableColumn<Book, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));

        TableColumn<Book, String> ownerCol = new TableColumn<>("Owner");
        ownerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOwnerName()));

        booksTable.getColumns().addAll(titleCol, authorCol, categoryCol, statusCol, ownerCol);

        // Action buttons
        HBox actionBox = new HBox(10);
        Button refreshButton = new Button("Refresh");
        Button deleteButton = new Button("Delete Book");
        Button closeButton = new Button("Close");

        refreshButton.setOnAction(e -> loadAllBooks(booksTable));
        deleteButton.setOnAction(e -> {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            if (selectedBook != null) {
                deleteBook(selectedBook, booksTable);
            } else {
                showAlert("Error", "Please select a book to delete");
            }
        });
        closeButton.setOnAction(e -> dialog.close());

        actionBox.getChildren().addAll(refreshButton, deleteButton, closeButton);

        content.getChildren().addAll(titleLabel, booksTable, actionBox);
        dialog.setScene(new Scene(content));

        // Load initial data
        loadAllBooks(booksTable);

        dialog.show();
    }

    private void loadAllUsers(TableView<User> usersTable) {
        apiClient.getAllUsers()
            .thenAccept(users -> {
                javafx.application.Platform.runLater(() -> {
                    usersTable.getItems().clear();
                    usersTable.getItems().addAll(users);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load users: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void loadAllBooks(TableView<Book> booksTable) {
        apiClient.getAllBooksAdmin()
            .thenAccept(books -> {
                javafx.application.Platform.runLater(() -> {
                    booksTable.getItems().clear();
                    booksTable.getItems().addAll(books);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load books: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void deleteUser(User user, TableView<User> usersTable) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete User");
        confirmation.setContentText("Are you sure you want to delete user: " + user.getFullName() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiClient.deleteUser(user.getId())
                    .thenAccept(success -> {
                        javafx.application.Platform.runLater(() -> {
                            if (success) {
                                showAlert("Success", "User deleted successfully");
                                loadAllUsers(usersTable);
                                // Refresh main tabs to show updated data
                                refreshBooksList();
                                if (!currentUser.isAdmin()) {
                                    refreshMyBooks();
                                }
                            } else {
                                showAlert("Error", "Failed to delete user");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Error deleting user: " + throwable.getMessage());
                        });
                        return null;
                    });
            }
        });
    }

    private void deleteBook(Book book, TableView<Book> booksTable) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Book");
        confirmation.setContentText("Are you sure you want to delete book: " + book.getTitle() + "?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                apiClient.deleteBookAdmin(book.getId())
                    .thenAccept(success -> {
                        javafx.application.Platform.runLater(() -> {
                            if (success) {
                                showAlert("Success", "Book deleted successfully");
                                loadAllBooks(booksTable);
                                // Refresh main tabs to show updated data
                                refreshBooksList();
                                if (!currentUser.isAdmin()) {
                                    refreshMyBooks();
                                }
                            } else {
                                showAlert("Error", "Failed to delete book");
                            }
                        });
                    })
                    .exceptionally(throwable -> {
                        javafx.application.Platform.runLater(() -> {
                            showAlert("Error", "Error deleting book: " + throwable.getMessage());
                        });
                        return null;
                    });
            }
        });
    }

    private void showUserActivityDialog() {
        Stage dialog = new Stage();
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("User Activity Monitor");
        dialog.setWidth(1000);
        dialog.setHeight(700);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        Label titleLabel = new Label("User Activity Monitor");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Filter controls
        HBox filterBox = new HBox(10);
        ComboBox<String> actionTypeFilter = new ComboBox<>();
        actionTypeFilter.getItems().addAll("All Actions", "BOOK_CREATED", "MESSAGE_SENT", "COMMENT_ADDED", "STATUS_CHANGED", "BOOK_BORROWED", "BOOK_RESERVED", "BOOK_RETURNED");
        actionTypeFilter.setValue("All Actions");

        TextField userNameFilter = new TextField();
        userNameFilter.setPromptText("Search by User Name (e.g. John Doe)");

        Button refreshButton = new Button("Refresh");
        Button exportButton = new Button("Export to Text");
        Button closeButton = new Button("Close");

        filterBox.getChildren().addAll(
            new Label("Action Type:"), actionTypeFilter,
            new Label("User Name:"), userNameFilter,
            refreshButton, exportButton, closeButton
        );

        // Activity table
        TableView<org.example.model.UserActionLog> activityTable = new TableView<>();

        TableColumn<org.example.model.UserActionLog, String> timestampCol = new TableColumn<>("Timestamp");
        timestampCol.setCellValueFactory(data -> {
            if (data.getValue().getTimestamp() != null) {
                return new javafx.beans.property.SimpleStringProperty(data.getValue().getTimestamp().toString());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        timestampCol.setPrefWidth(150);

        TableColumn<org.example.model.UserActionLog, String> userIdCol = new TableColumn<>("User ID");
        userIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserId()));
        userIdCol.setPrefWidth(80);

        TableColumn<org.example.model.UserActionLog, String> userCol = new TableColumn<>("User Name");
        userCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserName()));
        userCol.setPrefWidth(120);

        TableColumn<org.example.model.UserActionLog, String> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getActionType()));
        actionCol.setPrefWidth(120);

        TableColumn<org.example.model.UserActionLog, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        descriptionCol.setPrefWidth(350);

        TableColumn<org.example.model.UserActionLog, String> targetCol = new TableColumn<>("Target");
        targetCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTargetType()));
        targetCol.setPrefWidth(100);

        activityTable.getColumns().addAll(timestampCol, userIdCol, userCol, actionCol, descriptionCol, targetCol);

                // Event handlers
        refreshButton.setOnAction(e -> loadUserActivity(activityTable, actionTypeFilter.getValue(), userNameFilter.getText()));

        actionTypeFilter.setOnAction(e -> loadUserActivity(activityTable, actionTypeFilter.getValue(), userNameFilter.getText()));

        userNameFilter.setOnKeyReleased(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                loadUserActivity(activityTable, actionTypeFilter.getValue(), userNameFilter.getText());
            }
        });

        exportButton.setOnAction(e -> exportUserActivity(activityTable));
        closeButton.setOnAction(e -> dialog.close());

        content.getChildren().addAll(titleLabel, filterBox, activityTable);
        dialog.setScene(new Scene(content));

        // Load initial data
        loadUserActivity(activityTable, "All Actions", "");

        dialog.show();
    }

        private void loadUserActivity(TableView<org.example.model.UserActionLog> activityTable, String actionType, String userName) {
        CompletableFuture<List<org.example.model.UserActionLog>> future;

        // Always get all actions or filter by action type first
        if (actionType != null && !"All Actions".equals(actionType)) {
            future = apiClient.getActionsByType(actionType);
        } else {
            future = apiClient.getAllUserActions();
        }

        future.thenAccept(actions -> {
                javafx.application.Platform.runLater(() -> {
                    // Apply client-side user name filtering
                    List<org.example.model.UserActionLog> filteredActions = actions;

                    if (userName != null && !userName.trim().isEmpty()) {
                        String searchTerm = userName.trim().toLowerCase();
                        filteredActions = actions.stream()
                            .filter(action -> action.getUserName() != null &&
                                    action.getUserName().toLowerCase().contains(searchTerm))
                            .collect(java.util.stream.Collectors.toList());
                    }

                    activityTable.getItems().clear();
                    activityTable.getItems().addAll(filteredActions);
                });
            })
            .exceptionally(throwable -> {
                javafx.application.Platform.runLater(() -> {
                    showAlert("Error", "Failed to load user activity: " + throwable.getMessage());
                });
                return null;
            });
    }

    private void exportUserActivity(TableView<org.example.model.UserActionLog> activityTable) {
        StringBuilder sb = new StringBuilder();
        sb.append("USER ACTIVITY REPORT\n");
        sb.append("Generated: ").append(java.time.LocalDateTime.now()).append("\n\n");
                sb.append("Timestamp\t\tUser ID\t\tUser Name\t\tAction\t\tDescription\t\tTarget\n");
        sb.append("=".repeat(140)).append("\n");

        for (org.example.model.UserActionLog action : activityTable.getItems()) {
            sb.append(action.getTimestamp()).append("\t");
            sb.append(action.getUserId()).append("\t");
            sb.append(action.getUserName()).append("\t");
            sb.append(action.getActionType()).append("\t");
            sb.append(action.getDescription()).append("\t");
            sb.append(action.getTargetType()).append("\n");
        }

        // Create a dialog to show the exported text
        Stage exportDialog = new Stage();
        exportDialog.setTitle("Exported User Activity");
        exportDialog.setWidth(800);
        exportDialog.setHeight(600);

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        VBox exportContent = new VBox(10);
        exportContent.setPadding(new Insets(10));

        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(sb.toString());
            clipboard.setContent(content);
            showAlert("Success", "Activity report copied to clipboard");
        });

        exportContent.getChildren().addAll(textArea, copyButton);
        exportDialog.setScene(new Scene(exportContent));
        exportDialog.show();
    }

        private void loadCategoriesIntoCombo(ComboBox<String> categoryCombo) {
        apiClient.getBookCategories()
            .thenAccept(categories -> {
                javafx.application.Platform.runLater(() -> {
                    // Store current value
                    String currentValue = categoryCombo.getValue();
                    boolean hasAllOption = categoryCombo.getItems().contains("All Categories");

                    // Clear and rebuild
                    categoryCombo.getItems().clear();

                    // Add "All Categories" option if it was there before
                    if (hasAllOption) {
                        categoryCombo.getItems().add("All Categories");
                    }

                    // Add dynamic categories
                    categoryCombo.getItems().addAll(categories);

                    // Restore value or set default
                    if (currentValue != null && categoryCombo.getItems().contains(currentValue)) {
                        categoryCombo.setValue(currentValue);
                    } else if (hasAllOption) {
                        categoryCombo.setValue("All Categories");
                    }
                });
            })
            .exceptionally(throwable -> {
                // Categories loading failed, but combo already has fallback values
                System.err.println("Failed to load categories: " + throwable.getMessage());
                return null;
            });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
