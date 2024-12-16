package ru.hh.school.unittesting.homework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.hh.school.unittesting.homework.LibraryManager;
import ru.hh.school.unittesting.homework.NotificationService;
import ru.hh.school.unittesting.homework.UserService;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LibraryManagerTest {

  @Mock
  private UserService userService;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private LibraryManager libraryManager;

  @BeforeEach
  void setUp() {
    libraryManager.addBook("1984", 10);
    libraryManager.addBook("9999", 15);
    libraryManager.addBook("5252", 0);
  }

  @Test
  void testLibraryManagerAddBook() {
    libraryManager.addBook("1984", 10);
    int copies = libraryManager.getAvailableCopies("1984");

    assertEquals(20, copies);
  }

  @Test
  void testLibraryManagerGetAvailableCopies() {
    int copies = libraryManager.getAvailableCopies("1984");

    assertEquals(10, copies);
  }

  @Test
  void testBorrowBookByInactiveUser() {
    when(userService.isUserActive("777")).thenReturn(false);
    boolean isBorrowed = libraryManager.borrowBook("1984", "777");

    assertFalse(isBorrowed);
  }

  @Test
  void testBorrowMissingBook() {
    boolean isBorrowed = libraryManager.borrowBook("2517", "777");

    assertFalse(isBorrowed);
  }

  @Test
  void testBorrowUnavailableBook() {
    when(userService.isUserActive("777")).thenReturn(true);

    boolean isBorrowed = libraryManager.borrowBook("5252", "777");

    assertFalse(isBorrowed);
  }

  @ParameterizedTest
  @CsvSource({
      "1984, 777, 9",
      "9999, 123, 14"
  })
  void testBorrowBookSuccess(String bookId, String userId, int remaining) {
    when(userService.isUserActive(userId)).thenReturn(true);

    libraryManager.borrowBook(bookId, userId);

    int expectedRemaining = libraryManager.getAvailableCopies(bookId);

    assertEquals(remaining, expectedRemaining);
    assertTrue(libraryManager.borrowBook(bookId, userId));
  }

  @Test
  void testReturnNonBorrowedBook() {
    boolean returnResult = libraryManager.returnBook("1234", "777");

    assertFalse(returnResult);
  }

  @Test
  void textReturnBookFromAnotherUser() {
    when(userService.isUserActive("777")).thenReturn(true);

    libraryManager.borrowBook("1984", "777");
    boolean returnResult = libraryManager.returnBook("1984", "123");

    assertFalse(returnResult);
  }

  @Test
  void testReturnBookSuccess() {
    when(userService.isUserActive("777")).thenReturn(true);

    libraryManager.borrowBook("1984", "777");
    boolean returnResult = libraryManager.returnBook("1984", "777");
    Integer copies = libraryManager.getAvailableCopies("1984");

    assertEquals(10, copies);
    assertTrue(returnResult);
  }

  @Test
  void FeeCalculatorShouldThrowExceptionIfBookReturnedOnTime() {
    var exception = assertThrows(
        IllegalArgumentException.class,
        () -> libraryManager.calculateDynamicLateFee(-1, false, false)
    );
    assertEquals("Overdue days cannot be negative.", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
      "12, false, false, 6",
      "20, true, false, 15",
      "20, false, true, 8",
      "100, true, true, 60"
  })
  void testCalculateDynamicLateFee(
      int overdueDays,
      boolean isBestseller,
      boolean isPremiumMember,
      double expectedFee
  ) {
    double result = libraryManager.calculateDynamicLateFee(overdueDays, isBestseller, isPremiumMember);

    assertEquals(expectedFee, result);
  }
}