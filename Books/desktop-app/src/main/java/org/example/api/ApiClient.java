package org.example.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.model.Book;


import org.example.model.Comment;
import org.example.model.User;
import org.example.model.Message;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.util.List;

import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private static final String BASE_URL = System.getProperty("api.base.url", "http://localhost:8080/api");
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String authToken;

    public ApiClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // Authentication
    public CompletableFuture<User> login(String username, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    username, password
                );

                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Parse the response to get user info and token
                    var responseMap = objectMapper.readValue(response.body(), new TypeReference<java.util.Map<String, Object>>() {});
                    if (Boolean.TRUE.equals(responseMap.get("success"))) {
                        var userData = (java.util.Map<String, Object>) responseMap.get("user");
                        String token = (String) responseMap.get("token");

                        // Store the token for future requests
                        setAuthToken(token);

                        return objectMapper.convertValue(userData, User.class);
                    } else {
                        throw new RuntimeException("Login failed: " + responseMap.get("message"));
                    }
                } else {
                    throw new RuntimeException("Login failed: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error during login", e);
            }
        });
    }

    public void setAuthToken(String token) {
        this.authToken = token;
    }

    // Books API
    public CompletableFuture<List<Book>> getAllBooks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/books")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    // Parse the JSON response
                    return objectMapper.readValue(response.body(), new TypeReference<List<Book>>() {});
                } else {
                    throw new RuntimeException("Failed to get books: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting books", e);
            }
        });
    }

    // Unified search method with all filters
    public CompletableFuture<List<Book>> searchBooksUnified(String query, String category, String author,
                                                           String status, Integer yearFrom, Integer yearTo,
                                                           Boolean excludeCurrentUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder urlBuilder = new StringBuilder("/books/search?");
                boolean hasParams = false;

                if (query != null && !query.trim().isEmpty()) {
                    urlBuilder.append("query=").append(java.net.URLEncoder.encode(query.trim(), "UTF-8"));
                    hasParams = true;
                }

                if (category != null && !category.trim().isEmpty() && !"All Categories".equals(category)) {
                    if (hasParams) urlBuilder.append("&");
                    urlBuilder.append("category=").append(java.net.URLEncoder.encode(category.trim(), "UTF-8"));
                    hasParams = true;
                }

                if (author != null && !author.trim().isEmpty()) {
                    if (hasParams) urlBuilder.append("&");
                    urlBuilder.append("author=").append(java.net.URLEncoder.encode(author.trim(), "UTF-8"));
                    hasParams = true;
                }

                if (status != null && !status.trim().isEmpty() && !"All Statuses".equals(status)) {
                    if (hasParams) urlBuilder.append("&");
                    urlBuilder.append("status=").append(java.net.URLEncoder.encode(status.trim().toUpperCase(), "UTF-8"));
                    hasParams = true;
                }

                if (yearFrom != null) {
                    if (hasParams) urlBuilder.append("&");
                    urlBuilder.append("yearFrom=").append(yearFrom);
                    hasParams = true;
                }

                if (yearTo != null) {
                    if (hasParams) urlBuilder.append("&");
                    urlBuilder.append("yearTo=").append(yearTo);
                    hasParams = true;
                }

                if (excludeCurrentUser != null) {
                    if (hasParams) urlBuilder.append("&");
                    urlBuilder.append("excludeCurrentUser=").append(excludeCurrentUser);
                }

                HttpRequest request = createRequest(urlBuilder.toString())
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Book>>() {});
                } else {
                    throw new RuntimeException("Failed to search books: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error searching books", e);
            }
        });
    }





    public CompletableFuture<Boolean> reserveBook(String bookId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format("{\"userId\":\"%s\"}", userId);

                HttpRequest request = createRequest("/transactions/books/" + bookId + "/reserve")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200;
            } catch (Exception e) {
                throw new RuntimeException("Error reserving book", e);
            }
        });
    }

    public CompletableFuture<Boolean> returnBook(String bookId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format("{\"userId\":\"%s\"}", userId);

                HttpRequest request = createRequest("/transactions/books/" + bookId + "/return")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200;
            } catch (Exception e) {
                throw new RuntimeException("Error returning book", e);
            }
        });
    }

    public CompletableFuture<Boolean> borrowBook(String bookId, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format("{\"userId\":\"%s\"}", userId);

                HttpRequest request = createRequest("/transactions/books/" + bookId + "/borrow")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return true;
                } else {
                    throw new RuntimeException("Failed to borrow book: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error borrowing book", e);
            }
        });
    }

    public CompletableFuture<Book> addBook(String title, String author, String category, String description, String ownerId) {
        return addBook(title, author, category, description, null, ownerId);
    }

    public CompletableFuture<Book> addBook(String title, String author, String category, String description,
                                          Integer publicationYear, String ownerId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format(
                    "{\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"description\":\"%s\",\"publicationYear\":%s}",
                    title, author, category, description,
                    publicationYear != null ? publicationYear.toString() : "null"
                );

                HttpRequest request = createRequest("/books")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Book.class);
                } else {
                    throw new RuntimeException("Failed to add book: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error adding book", e);
            }
        });
    }

    public CompletableFuture<Book> updateBook(String bookId, String title, String author, String category, String description) {
        return updateBook(bookId, title, author, category, description, null);
    }

    public CompletableFuture<Book> updateBook(String bookId, String title, String author, String category, String description,
                                            Integer publicationYear) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format(
                    "{\"title\":\"%s\",\"author\":\"%s\",\"category\":\"%s\",\"description\":\"%s\",\"publicationYear\":%s}",
                    title, author, category, description,
                    publicationYear != null ? publicationYear.toString() : "null"
                );

                HttpRequest request = createRequest("/books/" + bookId)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Book.class);
                } else {
                    throw new RuntimeException("Failed to update book: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error updating book", e);
            }
        });
    }

    // Messages API


    public CompletableFuture<Message> sendMessage(String senderId, String bookId, String content, String parentMessageId) {
        return sendMessage(senderId, bookId, content, parentMessageId, null);
    }

    public CompletableFuture<Message> sendMessage(String senderId, String bookId, String content, String parentMessageId, String recipientId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody;
                if (recipientId != null) {
                    jsonBody = String.format(
                        "{\"bookId\":\"%s\",\"content\":\"%s\",\"parentMessageId\":\"%s\",\"recipientId\":\"%s\"}",
                        bookId, content, parentMessageId != null ? parentMessageId : "", recipientId
                    );
                } else {
                    jsonBody = String.format(
                        "{\"bookId\":\"%s\",\"content\":\"%s\",\"parentMessageId\":\"%s\"}",
                        bookId, content, parentMessageId != null ? parentMessageId : ""
                    );
                }

                HttpRequest request = createRequest("/messages")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Message.class);
                } else {
                    throw new RuntimeException("Failed to send message: " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error sending message", e);
            }
        });
    }

    public CompletableFuture<List<Message>> getUserMessages(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/messages/user/" + userId)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Message>>() {});
                } else {
                    throw new RuntimeException("Failed to get user messages: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting user messages", e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteMessage(String messageId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/messages/" + messageId)
                    .DELETE()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return true;
                } else {
                    throw new RuntimeException("Failed to delete message: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error deleting message", e);
            }
        });
    }

    // User API



        public CompletableFuture<List<Book>> getAllUserRelatedBooks(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/books/user/" + userId + "/all-related")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Book>>() {});
                } else {
                    throw new RuntimeException("Failed to get user-related books: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting user-related books", e);
            }
        });
    }



    public CompletableFuture<List<Book>> getAvailableBooksExcludingCurrentUser() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/books/available/exclude-current-user")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Book>>() {});
                } else {
                    throw new RuntimeException("Failed to get available books excluding current user: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting available books excluding current user", e);
            }
        });
    }









    // Admin API
    public CompletableFuture<List<User>> getAllUsers() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/admin/users")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<User>>() {});
                } else {
                    throw new RuntimeException("Failed to get all users: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting all users", e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteUser(String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/admin/users/" + userId)
                    .DELETE()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                throw new RuntimeException("Error deleting user", e);
            }
        });
    }

    public CompletableFuture<List<Book>> getAllBooksAdmin() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/admin/books")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Book>>() {});
                } else {
                    throw new RuntimeException("Failed to get all books: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting all books", e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteBookAdmin(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/admin/books/" + bookId)
                    .DELETE()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                throw new RuntimeException("Error deleting book", e);
            }
        });
    }



    public CompletableFuture<Boolean> deleteUserBook(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/books/" + bookId)
                    .DELETE()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200 || response.statusCode() == 204;
            } catch (Exception e) {
                throw new RuntimeException("Error deleting book", e);
            }
        });
    }

    // Categories API
    public CompletableFuture<List<String>> getBookCategories() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/books/categories")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<String>>() {});
                } else {
                    // Fallback to default categories matching our backend enum
                    return java.util.Arrays.asList("Fiction", "Non-Fiction", "Science Fiction", "Fantasy", "History", "Biography", "Romance", "Mystery", "Thriller", "Adventure");
                }
            } catch (Exception e) {
                // Fallback to default categories matching our backend enum
                return java.util.Arrays.asList("Fiction", "Non-Fiction", "Science Fiction", "Fantasy", "History", "Biography", "Romance", "Mystery", "Thriller", "Adventure");
            }
        });
    }

    // Admin user activity tracking methods
    public CompletableFuture<List<org.example.model.UserActionLog>> getAllUserActions() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/admin/user-actions")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<org.example.model.UserActionLog>>() {});
                } else {
                    throw new RuntimeException("Failed to get user actions: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting user actions", e);
            }
        });
    }





    public CompletableFuture<List<org.example.model.UserActionLog>> getActionsByType(String actionType) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/admin/actions-by-type?actionType=" + actionType)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<org.example.model.UserActionLog>>() {});
                } else {
                    throw new RuntimeException("Failed to get actions by type: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting actions by type", e);
            }
        });
    }

        // Helper method to create HTTP request with auth
    private HttpRequest.Builder createRequest(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json");

        // Add authorization header if token is available
        if (authToken != null) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        return builder;
    }

    // Comment methods
    public CompletableFuture<Comment> createComment(String bookId, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format("{\"content\":\"%s\"}", content);

                HttpRequest request = createRequest("/comments/books/" + bookId)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Comment.class);
                } else {
                    throw new RuntimeException("Failed to create comment: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error creating comment", e);
            }
        });
    }

    public CompletableFuture<Comment> createReply(String commentId, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format("{\"content\":\"%s\"}", content);

                HttpRequest request = createRequest("/comments/" + commentId + "/replies")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Comment.class);
                } else {
                    throw new RuntimeException("Failed to create reply: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error creating reply", e);
            }
        });
    }



    public CompletableFuture<List<Comment>> getNestedCommentsByBookId(String bookId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/comments/books/" + bookId + "/nested")
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), new TypeReference<List<Comment>>() {});
                } else {
                    throw new RuntimeException("Failed to get nested comments: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error getting nested comments", e);
            }
        });
    }



    public CompletableFuture<Comment> updateComment(String commentId, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String jsonBody = String.format("{\"content\":\"%s\"}", content);

                HttpRequest request = createRequest("/comments/" + commentId)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    return objectMapper.readValue(response.body(), Comment.class);
                } else {
                    throw new RuntimeException("Failed to update comment: " + response.statusCode());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error updating comment", e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteComment(String commentId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = createRequest("/comments/" + commentId)
                    .DELETE()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                return response.statusCode() == 200;
            } catch (Exception e) {
                throw new RuntimeException("Error deleting comment", e);
            }
        });
    }


















    // Book Status Management methods













    // Notification methods









    // Inner classes for requests
}
