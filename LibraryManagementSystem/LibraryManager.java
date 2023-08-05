package LibraryManagementSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class LibraryManager {
    public static void main(String[] noargs){
        String temp = new File("").getAbsolutePath();
        String books = temp + "\\Books\\";
        String borrowedBooks = temp + "\\BorrowedBooks\\";
        String auth = temp+"\\.users.txt";
        String lib = temp+"\\libr.txt";

        
        File b = new File(books);
        File bb = new File(borrowedBooks);
        File aut = new File(auth);
        File libr = new File(lib);
        if(!b.exists()){
            b.mkdir();
        }
        if(!bb.exists()){
            bb.mkdirs();
        }
        try{
            if(!aut.exists()){
                aut.createNewFile();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        try{
            if(!libr.exists()){
                libr.createNewFile();
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }


        Scanner inputScan = new Scanner(System.in);
        System.out.print("Login or Create new account? L or C (note cntrl+c in terimnal ends program at any time): ");
        String s = inputScan.nextLine();
        while(!s.toLowerCase().equals("l") && !s.toLowerCase().equals("c")){
            System.out.print("Wrong input: " + s + ", try again: L or C: ");
            s= inputScan.nextLine();
        }

        if(s.toLowerCase().equals("c")){
            System.out.println("Write User Name: ");
            s = inputScan.nextLine();
            while(loginExists(s,aut)){
                System.out.println("User Name already exists try again: ");
                s = inputScan.nextLine();
            }
            try{
            PrintWriter pw = new PrintWriter(new FileOutputStream(aut,true));
            String t = s;
            System.out.println("Write Password: ");
            s = inputScan.nextLine();
            String hash = t+" "+s;
            pw.println(hash);
            pw.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }else{
           System.out.println("Type your login: ");
           s = inputScan.nextLine();
           while(!loginExists(s,aut)){
                System.out.println("Login doesnt exist try again: ");
                s = inputScan.nextLine();
           }
           String user = s;
           System.out.println("Now type the password: ");
           s = inputScan.nextLine();
            try(Scanner fileReader = new Scanner(aut);)  {
            while(fileReader.hasNext()){
                String test = fileReader.nextLine();
                String backend = test.substring(test.indexOf(" ")+1, test.length());
                test = test.substring(0,test.indexOf(" "));
                System.out.println("test is: " + test);
                if(user.equals(test)) user = backend;
            }
            while(!s.equals(user)){
                System.out.println("Password doesnt match try again: ");
                s = inputScan.nextLine();
            }
                fileReader.close();
            }catch(Exception ex){
                ex.printStackTrace();;
            }
        }
        
        System.out.println("Welcome: ");
        System.out.println("Would you like to add a book(a)? Borrow a book(b)? or return a book(c)? ");
        s = inputScan.nextLine();
        s = s.toLowerCase();
        while(!s.equals("a") && !s.equals("b") && !s.equals("c")){
            System.out.println("Please choose a b or c: ");
            s = inputScan.nextLine();
            s = s.toLowerCase();
        }
        Library ind;
        try{
        FileInputStream filedip = new FileInputStream(libr);
        if(filedip.available() > 0){
            ObjectInputStream os = new ObjectInputStream(filedip);
            Object obj = os.readObject();
            os.close();
            ind = (Library)obj;
        }else{
            ind = new Library();
        }
        switch(s){
            case "a":
                addBook(inputScan, ind);
                break;
            case "b":
                borrowBook(inputScan, ind);
                break;
            case "c":
                returnBook(inputScan,ind);
                break;
        }


        FileOutputStream fs = new FileOutputStream(libr);
        ObjectOutputStream op = new ObjectOutputStream(fs);
        op.writeObject(ind);
        op.close();
        }catch(Exception ex){
            ex.printStackTrace();;
        }
        System.out.println("Thanks for visitng!");
        inputScan.close();
    }

    static void returnBook(Scanner in, Library l){
        System.out.println("What is the title of the book you are returning?");
        String s = in.nextLine();
        Book b = l.getTitle(s);
        while(b == null){
            System.out.println("Looks like that book never existed here, try another: ");
            s = in.nextLine();
            b = l.getTitle(s);
        }
        l.returnBook(b);
    }

    static void borrowBook(Scanner in, Library l){
        if(l.numOfBooks() <= 0){
            System.out.println("We dont have any books available");
            return;
        }
        System.out.println("What is the title of the book you want to borrow?");
        String s = in.nextLine();
        while(l.getTitle(s) == null){
             System.out.println("Looks like that title wasnt in our data base, try another: ");
             s = in.nextLine();
        }
        if(!l.getTitle(s).isAvailable()){
            System.out.println("Sorry, " + l.getTitle(s).getTitle() + " is not available for borrowing.");
            return;
        }
        System.out.println("Okay great and what is your name? ");
        Book bonk = l.getTitle(s);
        s = in.nextLine();
        String info;
        System.out.println("And lastly what is your contact info? ");
        info = in.nextLine();
        Patron p = new Patron(s, info);
        l.lendBook(bonk, p);

    }

    static void addBook(Scanner in, Library l){
        System.out.println("What is the title of the book you're adding? ");
        String s = in.nextLine();
        System.out.println("Who is the author of the book? ");
        String a = in.nextLine();
        System.out.println("What is the content of the book? ");
        String tot = in.nextLine();
        Book bok = new Book(s, a, tot);
        l.addBook(bok);
    }

    static boolean loginExists(String s, File f){
        try(Scanner fileReader = new Scanner(f);)  {
            while(fileReader.hasNext()){
                String test = fileReader.nextLine();
                test = test.substring(0,test.indexOf(" "));
                if(s.equals(test)) return true;
            }

            fileReader.close();
        }catch(Exception ex){
            ex.printStackTrace();;
        }
        return false;
    }
}


class Library implements Serializable{
    private List<Book> books;
    private List<Patron> patrons;
    

    public Library() {
        books = new ArrayList<>();
        patrons = new ArrayList<>();
    }

    public void addBook(Book book) {
        books.add(book);
    }

    public void addPatron(Patron patron) {
        patrons.add(patron);
    }

    public int numOfBooks(){
        return books.size();
    }
    public Book getTitle(String t){
        for(int i = 0; i < books.size(); i++){
            if(books.get(i).getTitle().equals(t)){
                return books.get(i);
            }
        }
        return null;
    }
    public void lendBook(Book book, Patron patron) {
        if (book.isAvailable()) {
            book.borrow();
            // Add logic to track borrowing history, due dates, etc.
            System.out.println(book.getTitle() + " has been borrowed by " + patron.getName());
        } else {
            System.out.println("Sorry, " + book.getTitle() + " is not available for borrowing.");
        }
    }

    public void returnBook(Book book) {
        if (!book.isAvailable()) {
            book.returnBook();
            // Add logic to update borrowing history, overdue checks, etc.
            System.out.println(book.getTitle() + " has been returned.");
        } else {
            System.out.println("This book is already returned.");
        }
    }

}

class Patron implements Serializable{
    private String name;
    private String contactInfo;

    public Patron(String name, String contactInfo) {
        this.name = name;
        this.contactInfo = contactInfo;
    }

    public String getName() {
        return name;
    }

    public String getContactInfo() {
        return contactInfo;
    }
}

class Book implements Serializable {
    private String title;
    private String author;
    private String isbn;
    private boolean available;

    Book(String t, String a, String i){
        this.title = t;
        this.author = a;
        this.isbn = i;
        this.available = true;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public boolean isAvailable() {
        return available;
    }

    public void borrow() {
        if (available) {
            available = false;
        } else {
            System.out.println("This book is already borrowed.");
        }
    }

    public void returnBook() {
        if (!available) {
            available = true;
        } else {
            System.out.println("This book is already returned.");
        }
    }

}
