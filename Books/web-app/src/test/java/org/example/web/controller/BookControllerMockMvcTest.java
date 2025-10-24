package org.example.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.example.web.service.BookService;
import org.example.web.model.Book;
import org.example.web.dto.CreateBookRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerMockMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    void getAllBooks_returnsList() throws Exception {
        Book bookA = new Book();
        bookA.setTitle("Book A");
        when(bookService.getAllBooks()).thenReturn(List.of(bookA));
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Book A")));
    }

    @Test
    void createBook_createsBook() throws Exception {
        Book bookC = new Book();
        bookC.setTitle("Book C");
        when(bookService.createBook(any(CreateBookRequest.class))).thenReturn(bookC);
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Book C\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Book C")));
    }

    @Test
    void searchBooks_returnsFilteredList() throws Exception {
        Book bookB = new Book();
        bookB.setTitle("Book B");
        when(bookService.searchBooksWithAllFilters(any(), any(), any(), any(), any(), any(), any())).thenReturn(List.of(bookB));
        mockMvc.perform(get("/books/search?query=Book B"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Book B")));
    }
}
