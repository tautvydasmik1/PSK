package org.example.web.config;

import org.example.web.service.UserService;
import org.example.web.service.BookService;
import org.example.web.model.Book;
import org.example.web.model.User;
import org.example.web.dto.CreateBookRequest;
import org.example.web.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Initializing Books Exchange System...");

        // Create default users
        userService.initializeDefaultUsers();
        System.out.println("✅ Default users created");

        // Create sample books for each user
        createSampleBooks();
        System.out.println("✅ Sample books created");

        System.out.println("🎉 System initialization complete!");
        System.out.println("📝 Default users:");
        System.out.println("   Admin: username=admin, password=admin");
        System.out.println("   User 1: username=user, password=password (Alice Johnson)");
        System.out.println("   User 2: username=user2, password=password2 (Bob Wilson)");
        System.out.println("   User 3: username=user3, password=password3 (Carol Davis)");
        System.out.println("🔐 All passwords are securely hashed using BCrypt");
        System.out.println("📚 3 sample books added for each user");
    }

    private void createSampleBooks() {
        // Create sample books for users, but only if that specific title doesn't already exist for the owner.
        User alice = userService.getUserByUsername("user");
        User bob = userService.getUserByUsername("user2");
        User carol = userService.getUserByUsername("user3");

        // Alice
        ensureBook(alice, "The Great Gatsby", "F. Scott Fitzgerald", Book.BookCategory.FICTION,
            "A classic American novel about the Jazz Age and the American Dream.", 1925);

        ensureBook(alice, "Pride and Prejudice", "Jane Austen", Book.BookCategory.ROMANCE,
            "A witty romance novel about manners, marriage, and social expectations.", 1813);

        ensureBook(alice, "The Catcher in the Rye", "J.D. Salinger", Book.BookCategory.FICTION,
            "A coming-of-age story following Holden Caulfield in New York City.", 1951);

        // Bob
        ensureBook(bob, "To Kill a Mockingbird", "Harper Lee", Book.BookCategory.FICTION,
            "A powerful story about racial injustice and the loss of innocence.", 1960);

        ensureBook(bob, "The Lord of the Rings", "J.R.R. Tolkien", Book.BookCategory.FANTASY,
            "An epic fantasy adventure about the quest to destroy the One Ring.", 1954);

        ensureBook(bob, "Dune", "Frank Herbert", Book.BookCategory.SCIENCE_FICTION,
            "A science fiction epic set on the desert planet Arrakis.", 1965);

        // Carol
        ensureBook(carol, "1984", "George Orwell", Book.BookCategory.SCIENCE_FICTION,
            "A dystopian novel about totalitarianism and surveillance society.", 1949);

        ensureBook(carol, "Harry Potter and the Philosopher's Stone", "J.K. Rowling", Book.BookCategory.FANTASY,
            "The first book in the magical Harry Potter series.", 1997);

        ensureBook(carol, "The Hunger Games", "Suzanne Collins", Book.BookCategory.THRILLER,
            "A dystopian novel about survival in a televised death match.", 2008);
    }

    private void ensureBook(User owner, String title, String author, Book.BookCategory category,
                            String description, Integer year) {
        if (owner == null) return; // user missing

        boolean exists = bookRepository.existsByTitleAndOwnerId(title, owner.getId());
        if (exists) {
            System.out.println("📚 Sample book already exists for user " + owner.getUsername() + ": " + title);
            return;
        }

        createBookForUser(owner, title, author, category, description, year);
        System.out.println("➕ Created sample book for " + owner.getUsername() + ": " + title);
    }

    private void createBookForUser(User owner, String title, String author, Book.BookCategory category,
                                 String description, Integer year) {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle(title);
        request.setAuthor(author);
        request.setCategory(category);
        request.setDescription(description);
        request.setOwnerId(owner.getId());
        request.setPublicationYear(year);
        request.setStatus(Book.BookStatus.AVAILABLE);

        // Use service method which automatically logs the action
        bookService.createBook(request);
    }
}
