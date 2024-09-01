import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        //these libraries used for creating pattern with 3 zeroes after point for double number.
        //Local.ENGLISH using for creating point instead of common
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        DecimalFormat df = new DecimalFormat("0.000", decimalFormatSymbols);
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.nextLine();
        // I create bankingSystem using Singleton pattern, for that ensure that I have only one Instance of bankingSystem
        BankingSystem bankingSystemInstance = BankingSystem.getInstance();
        // I use Decorator pattern, using this I catch error, which I throw in BankingSystem class and write according message
        BankingSystemDecorator bankingSystem = new BankingSystemDecorator(bankingSystemInstance);
        for (int i = 0; i < n; i++) {
            String str = sc.nextLine();
            String[] strings = str.split(" ");
            if (strings[0].equals("Create") && strings[1].equals("Account")) {
                if (strings[2].equals("Savings")) {
                    //create SavingAccount and add in history this step
                    SavingAccount savingAccount = new SavingAccount(strings[3], Double.parseDouble(strings[4]));
                    savingAccount.addEventInHistory("Initial Deposit $" + df.format(Double.parseDouble(strings[4])));
                    bankingSystem.createAccount(strings[3], savingAccount);
                } else if (strings[2].equals("Checking")) {
                    //create CheckingAccount and add in history this step
                    CheckingAccount checkingAccount = new CheckingAccount(strings[3], Double.parseDouble(strings[4]));
                    checkingAccount.addEventInHistory("Initial Deposit $" + df.format(Double.parseDouble(strings[4])));
                    bankingSystem.createAccount(strings[3], checkingAccount);
                } else if (strings[2].equals("Business")) {
                    //create BusinessAccount and add in history this step
                    BusinessAccount businessAccount = new BusinessAccount(strings[3], Double.parseDouble(strings[4]));
                    businessAccount.addEventInHistory("Initial Deposit $" + df.format(Double.parseDouble(strings[4])));
                    bankingSystem.createAccount(strings[3], businessAccount);
                }
                System.out.println("A new " + strings[2] + " account created for " + strings[3] + " with an initial balance of $" + df.format(Double.parseDouble(strings[4])) + ".");
                //all next method are described in BankingSystem class
            } else if (strings[0].equals("Deposit")) {
                double depositAmount = Double.parseDouble(strings[2]);
                bankingSystem.deposit(strings[1], depositAmount);
            } else if (strings[0].equals("Withdraw")) {
                double sumAmount = Double.parseDouble(strings[2]);
                bankingSystem.withdraw(strings[1], sumAmount);
            } else if (strings[0].equals("Transfer")) {
                bankingSystem.transfer(strings[1], strings[2], Double.parseDouble(strings[3]));
            } else if (strings[0].equals("View")) {
                bankingSystem.view(strings[1]);
            } else if (strings[0].equals("Deactivate")) {
                bankingSystem.deactivate(strings[1]);
            } else if (strings[0].equals("Activate")) {
                bankingSystem.activate(strings[1]);
            }
        }
    }
}

//interface, which created for BankingSystemDecorator, where I used decorator realisations of this interface
interface BankingSystemMethods {
    void createAccount(String name, Account account);
    void deposit(String name, double depositAmount);
    void withdraw(String name, double sumAmount);
    void transfer(String nameFrom, String nameTo, double transferAmount);
    void deactivate(String name);
    void activate(String name);
    void view(String name);
}

//class BankingSystem, where I use Singleton pattern for that ensure that I have only one instance of banking system
//I use realization "Class Holder Singleton" from javarush, as it is most fast and popular realization, also has thread safety
class BankingSystem implements BankingSystemMethods {
    //these libraries used for creating pattern with 3 zeroes after point for double number.
    //Local.ENGLISH using for creating point instead of common
    DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
    DecimalFormat df = new DecimalFormat("0.000", decimalFormatSymbols);
    // map of accounts, where key is name of account for cozy searching
    HashMap<String, Account> accounts = new HashMap<>();
    //for creating account I just adding it in map
    @Override
    public void createAccount(String name, Account account) {
        accounts.put(name, account);
    }
    @Override
    public void deposit(String name, double depositAmount) {
        // if I don't have this account I throw error, which catch in Decorator class
        if (accounts.containsKey(name)) {
            Account account = accounts.get(name);
            // using instanceof I check type of Account and change information for this class, also add in history info about operation
            if (account instanceof SavingAccount) {
                ((SavingAccount) account).depDeposit(depositAmount);
                ((SavingAccount) account).addEventInHistory("Deposit $" + df.format(depositAmount));
                System.out.println(name + " successfully deposited $" + df.format(depositAmount) + ". New Balance: $" + df.format(((SavingAccount) account).getBalance()) + ".");
            } else if (account instanceof CheckingAccount) {
                ((CheckingAccount) account).depDeposit(depositAmount);
                ((CheckingAccount) account).addEventInHistory("Deposit $" + df.format(depositAmount));
                System.out.println(name + " successfully deposited $" + df.format(depositAmount) + ". New Balance: $" + df.format(((CheckingAccount) account).getBalance()) + ".");
            } else if (account instanceof BusinessAccount) {
                ((BusinessAccount) account).depDeposit(depositAmount);
                ((BusinessAccount) account).addEventInHistory("Deposit $" + df.format(depositAmount));
                System.out.println(name + " successfully deposited $" + df.format(depositAmount) + ". New Balance: $" + df.format(((BusinessAccount) account).getBalance()) + ".");
            }
        } else {
            throw new EmptyStackException();
        }
    }
    @Override
    public void withdraw(String name, double sumAmount) {
        // if I don't have this account I throw error, which catch in Decorator class
        if (accounts.containsKey(name)) {
            Account account = accounts.get(name);
            // using instanceof I check type of Account and change information for this class, also add in history info about operation
            if (account instanceof SavingAccount) {
                // I check if account is inactive, I throw error, as cannot withdraw money from this account, and catch Error in decorator class
                if (((SavingAccount) account).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException("Inacive account");
                // I check if Peron withdraw more money than have I throw the Error, which catch in decorator class
                } else if (((SavingAccount) account).getBalance() < sumAmount) {
                    throw new Error();
                } else {
                    //in withdraw method I compute fee, using Strategy pattern, I compute personal fee for every type of account
                    double fee = ((SavingAccount) account).withDraw(sumAmount);
                    ((SavingAccount) account).addEventInHistory("Withdrawal $" + df.format(sumAmount));
                    System.out.println(name + " successfully withdrew $" + df.format(sumAmount - fee) + ". New Balance: $" +
                            df.format(((SavingAccount) account).getBalance()) + "." + " Transaction Fee: $" + df.format(fee) + " (1.5%) in the system.");
                }
            } else if (account instanceof CheckingAccount) {
                // I check if account is inactive, I throw error, as cannot withdraw money from this account, and catch Error in decorator class
                if (((CheckingAccount) account).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException("Inacive account");
                    // I check if Peron withdraw more money than have I throw the Error, which catch in decorator class
                } else if (((CheckingAccount) account).getBalance() < sumAmount) {
                    throw new Error();
                } else {
                    //in withdraw method I compute fee, using Strategy pattern, I compute personal fee for every type of account
                    double fee = ((CheckingAccount) account).withDraw(sumAmount);
                    ((CheckingAccount) account).addEventInHistory("Withdrawal $" + df.format(sumAmount));
                    System.out.println(name + " successfully withdrew $" + df.format(sumAmount - fee)  + ". New Balance: $" +
                            df.format(((CheckingAccount) account).getBalance()) + "." + " Transaction Fee: $" + df.format(fee) + " (2.0%) in the system.");
                }
            } else if (account instanceof BusinessAccount) {
                // I check if account is inactive, I throw error, as cannot withdraw money from this account, and catch Error in decorator class
                if (((BusinessAccount) account).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException("Inactive account");
                    // I check if Peron withdraw more money than have I throw the Error, which catch in decorator class
                } else if (((BusinessAccount) account).getBalance() < sumAmount) {
                    throw new Error();
                } else {
                    //in withdraw method I compute fee, using Strategy pattern, I compute personal fee for every type of account
                    double fee = ((BusinessAccount) account).withDraw(sumAmount);
                    ((BusinessAccount) account).addEventInHistory("Withdrawal $" + df.format(sumAmount));
                    System.out.println(name + " successfully withdrew $" + df.format(sumAmount - fee)  + ". New Balance: $" +
                            df.format(((BusinessAccount) account).getBalance()) + "." + " Transaction Fee: $" + df.format(fee) + " (2.5%) in the system.");
                }
            }
        } else {
            throw new EmptyStackException();
        }
    }
    @Override
    public void transfer(String nameFrom, String nameTo, double transferAmount) {
        Account accountFrom = accounts.get(nameFrom);
        Account accountTo = accounts.get(nameTo);
        // if I don't have one of these accounts I throw error, which catch in Decorator class
        if (!accounts.containsKey(nameFrom)) {
            throw new EmptyStackException();
        } else if (!accounts.containsKey(nameTo)) {
            throw new ExceptionInInitializerError();
        } else {
            if (accountFrom instanceof SavingAccount) {
                // I check if account of person who send money is inactive, I throw error, as cannot withdraw money from this account, and catch Error in decorator class
                if (((SavingAccount) accountFrom).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException();
                // I check if person who send money, don't have so much money I throw the Error which I catch in decorator class
                } else if (((SavingAccount) accountFrom).getBalance() < transferAmount) {
                    throw new Error();
                } else {
                    //in withdraw method I compute fee, using Strategy pattern, I compute personal fee for every type of account
                    double fee = ((SavingAccount) accountFrom).withDraw(transferAmount);
                    if (accountTo instanceof SavingAccount) {
                        ((SavingAccount) accountTo).depDeposit(transferAmount - fee);
                    } else if (accountTo instanceof CheckingAccount) {
                        ((CheckingAccount) accountTo).depDeposit(transferAmount - fee);
                    } else if (accountTo instanceof BusinessAccount) {
                        ((BusinessAccount) accountTo).depDeposit(transferAmount - fee);
                    }
                    ((SavingAccount) accountFrom).addEventInHistory("Transfer $" + df.format(transferAmount));
                    System.out.println(nameFrom + " successfully transferred $" + df.format(transferAmount - fee)  + " to " + nameTo +
                            ". New Balance: $" + df.format(((SavingAccount) accountFrom).getBalance()) + ". Transaction Fee: $" +
                            df.format(fee) + " (1.5%) in the system.");
                }
            } else if (accountFrom instanceof CheckingAccount) {
                // I check if account of person who send money is inactive, I throw error, as cannot withdraw money from this account, and catch Error in decorator class
                if (((CheckingAccount) accountFrom).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException();
                    // I check if person who send money, don't have so much money I throw the Error which I catch in decorator class
                } else if (((CheckingAccount) accountFrom).getBalance() < transferAmount) {
                    throw new Error();
                } else {
                    //in withdraw method I compute fee, using Strategy pattern, I compute personal fee for every type of account
                    double fee = ((CheckingAccount) accountFrom).withDraw(transferAmount);
                    if (accountTo instanceof SavingAccount) {
                        ((SavingAccount) accountTo).depDeposit(transferAmount - fee);
                    } else if (accountTo instanceof CheckingAccount) {
                        ((CheckingAccount) accountTo).depDeposit(transferAmount - fee);
                    } else if (accountTo instanceof BusinessAccount) {
                        ((BusinessAccount) accountTo).depDeposit(transferAmount - fee);
                    }
                    ((CheckingAccount) accountFrom).addEventInHistory("Transfer $" + df.format(transferAmount));
                    System.out.println(nameFrom + " successfully transferred $" + df.format(transferAmount - fee) + " to " + nameTo +
                            ". New Balance: $" + df.format(((CheckingAccount) accountFrom).getBalance()) + ". Transaction Fee: $" +
                            df.format(fee) + " (2.0%) in the system.");
                }
            } else if (accountFrom instanceof BusinessAccount) {
                // I check if account of person who send money is inactive, I throw error, as cannot withdraw money from this account, and catch Error in decorator class
                if (((BusinessAccount) accountFrom).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException();
                    // I check if person who send money, don't have so much money I throw the Error which I catch in decorator class
                } else if (((BusinessAccount) accountFrom).getBalance() < transferAmount) {
                    throw new Error();
                } else {
                    //in withdraw method I compute fee, using Strategy pattern, I compute personal fee for every type of account
                    double fee = ((BusinessAccount) accountFrom).withDraw(transferAmount);
                    if (accountTo instanceof SavingAccount) {
                        ((SavingAccount) accountTo).depDeposit(transferAmount - fee);
                    } else if (accountTo instanceof CheckingAccount) {
                        ((CheckingAccount) accountTo).depDeposit(transferAmount - fee);
                    } else if (accountTo instanceof BusinessAccount) {
                        ((BusinessAccount) accountTo).depDeposit(transferAmount - fee);
                    }
                    ((BusinessAccount) accountFrom).addEventInHistory("Transfer $" + df.format(transferAmount));
                    System.out.println(nameFrom + " successfully transferred $" + df.format(transferAmount - fee) + " to " + nameTo +
                            ". New Balance: $" + df.format(((BusinessAccount) accountFrom).getBalance()) + ". Transaction Fee: $" +
                            df.format(fee) + " (2.5%) in the system.");
                }
            }
        }
    }
    @Override
    public void deactivate(String name) {
        // if I don't have this account I throw error, which catch in Decorator class
        if (accounts.containsKey(name)) {
            Account account = accounts.get(name);
            if (account instanceof SavingAccount) {
                // I check if account is already inactive I throw Error, which I catch in decorator class
                if (((SavingAccount) account).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException();
                } else {
                    ((SavingAccount) account).deactivate();
                    System.out.println(name + "'s account is now deactivated.");
                }
            } else if (account instanceof CheckingAccount) {
                // I check if account is already inactive I throw Error, which I catch in decorator class
                if (((CheckingAccount) account).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException();
                } else {
                    ((CheckingAccount) account).deactivate();
                    System.out.println(name + "'s account is now deactivated.");
                }
            } else if (account instanceof BusinessAccount) {
                // I check if account is already inactive I throw Error, which I catch in decorator class
                if (((BusinessAccount) account).getState() == AccountStates.INACTIVE) {
                    throw new NullPointerException();
                } else {
                    ((BusinessAccount) account).deactivate();
                    System.out.println(name + "'s account is now deactivated.");
                }
            }
        } else {
            throw new EmptyStackException();
        }
    }
    @Override
    public void activate(String name) {
        // if I don't have this account I throw error, which catch in Decorator class
        if (accounts.containsKey(name)) {
            Account account = accounts.get(name);
            if (account instanceof SavingAccount) {
                // I check if account is already active I throw Error, which I catch in decorator class
                if (((SavingAccount) account).getState() == AccountStates.ACTIVE) {
                    throw new NullPointerException();
                } else {
                    ((SavingAccount) account).activate();
                    System.out.println(name + "'s account is now activated.");
                }
            } else if (account instanceof CheckingAccount) {
                // I check if account is already active I throw Error, which I catch in decorator class
                if (((CheckingAccount) account).getState() == AccountStates.ACTIVE) {
                    throw new NullPointerException();
                } else {
                    ((CheckingAccount) account).activate();
                    System.out.println(name + "'s account is now activated.");
                }
            } else if (account instanceof BusinessAccount) {
                // I check if account is already active I throw Error, which I catch in decorator class
                if (((BusinessAccount) account).getState() == AccountStates.ACTIVE) {
                    throw new NullPointerException();
                } else {
                    ((BusinessAccount) account).activate();
                    System.out.println(name + "'s account is now activated.");
                }
            }
        } else {
            throw new EmptyStackException();
        }
    }

    @Override
    public void view(String name) {
        // if I don't have this account I throw error, which catch in Decorator class
        if (accounts.containsKey(name)) {
            Account account = accounts.get(name);
            String state = "";
            if (account instanceof SavingAccount) {
                // I create string state, as in getState() I get state of account from enum
                if (((SavingAccount) account).getState() == AccountStates.ACTIVE) {
                    state = "Active";
                } else {
                    state = "Inactive";
                }
                System.out.print(name + "'s Account: Type: Savings, Balance: $" + df.format(((SavingAccount) account).getBalance()) +
                        ", State: " + state + ", Transactions: [");
                ArrayList<String> transactions = ((SavingAccount) account).getTransactionEventList();
                for (int i = 0; i < transactions.size(); i++) {
                    if (i == transactions.size() - 1) {
                        System.out.println(transactions.get(i) + "].");
                    } else {
                        System.out.print(transactions.get(i) + ", ");
                    }
                }
            } else if (account instanceof CheckingAccount) {
                // I create string state, as in getState() I get state of account from enum
                if (((CheckingAccount) account).getState() == AccountStates.ACTIVE) {
                    state = "Active";
                } else {
                    state = "Inactive";
                }
                System.out.print(name + "'s Account: Type: Checking, Balance: $" + df.format(((CheckingAccount) account).getBalance()) +
                        ", State: " + state + ", Transactions: [");
                ArrayList<String> transactions = ((CheckingAccount) account).getTransactionEventList();
                for (int i = 0; i < transactions.size(); i++) {
                    if (i == transactions.size() - 1) {
                        System.out.println(transactions.get(i) + "].");
                    } else {
                        System.out.print(transactions.get(i) + ", ");
                    }
                }
            } else if (account instanceof BusinessAccount) {
                // I create string state, as in getState() I get state of account from enum
                if (((BusinessAccount) account).getState() == AccountStates.ACTIVE) {
                    state = "Active";
                } else {
                    state = "Inactive";
                }
                System.out.print(name + "'s Account: Type: Business, Balance: $" + df.format(((BusinessAccount) account).getBalance()) +
                        ", State: " + state + ", Transactions: [");
                ArrayList<String> transactions = ((BusinessAccount) account).getTransactionEventList();
                for (int i = 0; i < transactions.size(); i++) {
                    if (i == transactions.size() - 1) {
                        System.out.println(transactions.get(i) + "].");
                    } else {
                        System.out.print(transactions.get(i) + ", ");
                    }
                }
            }
        } else {
            throw new EmptyStackException();
        }
    }

    private BankingSystem() {
    }
    // using final it guarantees that I create only one instance of BankingSystem
    private static class HolderOfBankingSystem {
        public static final BankingSystem HOLDER_INSTANCE = new BankingSystem();
    }

    public static BankingSystem getInstance() {
        return HolderOfBankingSystem.HOLDER_INSTANCE;
    }
}   

//Decorator class, where I catch Errors in right order for output
class BankingSystemDecorator implements BankingSystemMethods {

    //decorator realization of the interface
    protected BankingSystemMethods errorHandling;

    public BankingSystemDecorator (BankingSystemMethods errorHandling) {
        this.errorHandling = errorHandling;
    }
    //for creating account I don't have errors
    @Override
    public void createAccount(String name, Account account) {
        errorHandling.createAccount(name, account);
    }

    @Override
    public void deposit(String name, double depositAmount) {
        try {
            errorHandling.deposit(name, depositAmount);
        //catch error if account doesn't exist
        } catch (EmptyStackException e) {
            System.out.println("Error: Account " + name + " does not exist.");
        }
    }

    @Override
    public void withdraw(String name, double sumAmount) {
        try {
            errorHandling.withdraw(name, sumAmount);
            //catch error if account doesn't exist
        } catch (EmptyStackException e) {
            System.out.println("Error: Account " + name + " does not exist.");
            //catch error if account inactive
        } catch (NullPointerException e) {
            System.out.println("Error: Account " + name + " is inactive.");
            //catch error if person withdraw more money than have
        } catch (Error e) {
            System.out.println("Error: Insufficient funds for " + name + ".");
        }
    }

    @Override
    public void transfer(String nameFrom, String nameTo, double transferAmount) {
        try {
            errorHandling.transfer(nameFrom, nameTo, transferAmount);
            //catch error if account doesn't exist
        } catch (EmptyStackException e) {
            System.out.println("Error: Account " + nameFrom + " does not exist.");
            //catch error if account doesn't exist
        } catch (ExceptionInInitializerError e) {
            System.out.println("Error: Account " + nameTo + " does not exist.");
            //catch error if account inactive
        } catch (NullPointerException e) {
            System.out.println("Error: Account " + nameFrom + " is inactive.");
            //catch error if person send more money than have
        } catch (Error e) {
            System.out.println("Error: Insufficient funds for " + nameFrom + ".");
        }
    }

    @Override
    public void deactivate(String name) {
        try {
            errorHandling.deactivate(name);
            //catch error if account doesn't exist
        } catch (EmptyStackException e) {
            System.out.println("Error: Account " + name + " does not exist.");
            //catch error if account is already inactive
        } catch (NullPointerException e) {
            System.out.println("Error: Account " + name + " is already deactivated.");
        }
    }

    @Override
    public void activate(String name) {
        try {
            errorHandling.activate(name);
            //catch error if account doesn't exist
        } catch (EmptyStackException e) {
            System.out.println("Error: Account " + name + " does not exist.");
            //catch error if account is already active
        } catch (NullPointerException e) {
            System.out.println("Error: Account " + name + " is already activated.");
        }
    }

    @Override
    public void view(String name) {
        try {
            errorHandling.view(name);
            //catch error if account doesn't exist
        } catch (EmptyStackException e) {
            System.out.println("Error: Account " + name + " does not exist.");
        }
    }
}
//interface of Strategy pattern, as transactionFee is personal for every account
interface TransactionFeeStrategy {
    double transactionFee(double amountOfTransaction);
}
//in this class I compute fee for SavingAccount
class SavingAccountFee implements TransactionFeeStrategy {
    @Override
    public double transactionFee(double amountOfTransaction) {
        return amountOfTransaction * 0.015;
    }
}
//in this class I compute fee for CheckingAccount
class CheckingAccountFee implements TransactionFeeStrategy {
    @Override
    public double transactionFee(double amountOfTransaction) {
        return amountOfTransaction * 0.02;
    }
}
//in this class I compute fee for BusinessAccount
class BusinessAccountFee implements TransactionFeeStrategy {
    @Override
    public double transactionFee(double amountOfTransaction) {
        return amountOfTransaction * 0.025;
    }
}
// actions of account for change states
interface AccountActions {
    void deactivate();
    void activate();
}
//it's pure implementation of State pattern, I use enum, as it's more useful for this program
enum AccountStates {
    INACTIVE,
    ACTIVE
}

//abstract class, which used for adding all types of accounts in one hashmap
//Also in this class I create instance of TransactionFeeStrategy and in derivative classes I assign type of account to this instance
abstract class Account {
    TransactionFeeStrategy transactionFeeStrategy;
}


class SavingAccount extends Account implements AccountActions {
    private ArrayList<String> transactionEventList = new ArrayList<>();
    private String accountName;
    private double balance;
    private AccountStates state;
    //constructor for creating SavingAccount
    public SavingAccount(String name, double dep) {
        this.accountName = name;
        this.balance = dep;
        //active account by default
        this.state = AccountStates.ACTIVE;
    }
    //getters and setters for private variables
    public void addEventInHistory(String event) {
        this.transactionEventList.add(event);
    }
    public ArrayList<String> getTransactionEventList() {
        return transactionEventList;
    }
    public AccountStates getState() {
        return this.state;
    }
    //adding money to account
    public void depDeposit(double dep) {
        this.balance += dep;
    }
    public double getBalance() {
        return balance;
    }
    //I return value of fee in this method
    public double withDraw(double sum) {
        this.balance -= sum;
        //using Strategy pattern I create instance of SavingAccount and get fee for this type
        transactionFeeStrategy = new SavingAccountFee();
        return transactionFeeStrategy.transactionFee(sum);
    }

    @Override
    public void deactivate() {
        this.state = AccountStates.INACTIVE;
    }

    @Override
    public void activate() {
        this.state = AccountStates.ACTIVE;
    }
}
class CheckingAccount extends Account implements AccountActions {
    private ArrayList<String> transactionEventList = new ArrayList<>();
    private String accountName;
    private double balance;
    private AccountStates state;
    public CheckingAccount(String name, double dep) {
        this.accountName = name;
        this.balance = dep;
        //active acount by default
        this.state = AccountStates.ACTIVE;
    }
    //getters and setters for private variables
    public void addEventInHistory(String event) {
        this.transactionEventList.add(event);
    }
    public ArrayList<String> getTransactionEventList() {
        return transactionEventList;
    }
    //adding money to account
    public void depDeposit(double dep) {
        this.balance += dep;
    }
    public double getBalance() {
        return balance;
    }
    //I return value of fee in this method
    public double withDraw(double sum) {
        this.balance -= sum;
        //using Strategy pattern I create instance of CheckingAccount and get fee for this type
        transactionFeeStrategy = new CheckingAccountFee();
        return transactionFeeStrategy.transactionFee(sum);
    }
    public AccountStates getState() {
        return this.state;
    }
    @Override
    public void deactivate() {
        this.state = AccountStates.INACTIVE;
    }

    @Override
    public void activate() {
        this.state = AccountStates.ACTIVE;
    }
}
class BusinessAccount extends Account implements AccountActions {
    private ArrayList<String> transactionEventList = new ArrayList<>();
    private String accountName;
    private double balance;
    private AccountStates state;
    public BusinessAccount(String name, double dep) {
        this.accountName = name;
        this.balance = dep;
        this.state = AccountStates.ACTIVE;
    }
    //getters and setters for private variables
    public void addEventInHistory(String event) {
        this.transactionEventList.add(event);
    }
    public ArrayList<String> getTransactionEventList() {
        return transactionEventList;
    }
    //add money to account
    public void depDeposit(double dep) {
        this.balance += dep;
    }
    public double getBalance() {
        return balance;
    }
    //I return value of fee in this method
    public double withDraw(double sum) {
        this.balance -= sum;
        //using Strategy pattern I create instance of BusinessAccount and get fee for this type
        transactionFeeStrategy = new BusinessAccountFee();
        return transactionFeeStrategy.transactionFee(sum);
    }
    public AccountStates getState() {
        return this.state;
    }
    @Override
    public void deactivate() {
        this.state = AccountStates.INACTIVE;
    }

    @Override
    public void activate() {
        this.state = AccountStates.ACTIVE;
    }
}
