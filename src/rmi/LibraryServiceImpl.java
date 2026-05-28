package rmi;

import db.BookDAO;
import db.LoanDAO;
import model.Book;
import model.Loan;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RMI Server Implementation.
 * Extends UnicastRemoteObject to be exportable as a remote object.
 */
public class LibraryServiceImpl extends UnicastRemoteObject implements LibraryService {

    private final BookDAO bookDAO;
    private final LoanDAO loanDAO;

    public LibraryServiceImpl() throws RemoteException {
        super();
        this.bookDAO = new BookDAO();
        this.loanDAO = new LoanDAO();
    }

    @Override
    public List<Book> searchBook(String title) throws RemoteException {
        try {
            return bookDAO.searchBooks(title);
        } catch (SQLException e) {
            System.err.println("[RMI] searchBook error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public int checkAvailability(int bookId) throws RemoteException {
        try {
            Book book = bookDAO.getBookById(bookId);
            return (book != null) ? book.getAvailableCopies() : -1;
        } catch (SQLException e) {
            System.err.println("[RMI] checkAvailability error: " + e.getMessage());
            return -1;
        }
    }

    @Override
    public List<Loan> getMemberLoans(int memberId) throws RemoteException {
        try {
            return loanDAO.getLoansByMember(memberId);
        } catch (SQLException e) {
            System.err.println("[RMI] getMemberLoans error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public int getTotalBooks() throws RemoteException {
        try {
            return bookDAO.getTotalBooks();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public String ping() throws RemoteException {
        return "Smart Library RMI Server is running — " + java.time.LocalDateTime.now();
    }
}
