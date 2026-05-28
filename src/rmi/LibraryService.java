package rmi;

import model.Book;
import model.Loan;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * RMI Remote Interface — defines methods exposed by the RMI server.
 * Clients call these methods as if they were local.
 */
public interface LibraryService extends Remote {

    /**
     * Searches for books by title keyword.
     * @param title partial or full title to search
     * @return list of matching books
     */
    List<Book> searchBook(String title) throws RemoteException;

    /**
     * Checks how many copies of a book are available.
     * @param bookId the book's primary key
     * @return number of available copies, or -1 if not found
     */
    int checkAvailability(int bookId) throws RemoteException;

    /**
     * Returns all active/overdue loans for a given member.
     * @param memberId the member's primary key
     * @return list of loans
     */
    List<Loan> getMemberLoans(int memberId) throws RemoteException;

    /**
     * Returns total number of books in the library.
     */
    int getTotalBooks() throws RemoteException;

    /**
     * Returns a ping response to verify the server is alive.
     */
    String ping() throws RemoteException;
}
