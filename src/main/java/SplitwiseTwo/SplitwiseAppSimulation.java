package SplitwiseTwo;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

// --- Enums ---

enum SplitType {
    EQUAL, EXACT, PERCENTAGE, SHARE
}

// --- Core Data Classes ---

@Getter
class User {
    private final String id;
    private final String name;
    private final String email;

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "User{" + "id='" + id + '\'' + ", name='" + name + '\'' + '}';
    }
}

class Group {
    @Getter
    private final String id;
    @Getter
    private final String name;
    private final List<User> members; // Use CopyOnWriteArrayList for thread safety if members can be added/removed concurrently

    public Group(String id, String name, List<User> members) {
        this.id = id;
        this.name = name;
        this.members = new CopyOnWriteArrayList<>(members);
    }

    public List<User> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
        }
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    @Override
    public String toString() {
        return "Group{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", members=" + members.stream().map(User::getName).collect(Collectors.joining(", ")) + '}';
    }
}

@Getter
class Transaction {
    private final User payer;
    private final User owedBy;
    private final double amount;

    public Transaction(User payer, User owedBy, double amount) {
        this.payer = payer;
        this.owedBy = owedBy;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return owedBy.getName() + " owes " + payer.getName() + " $" + String.format("%.2f", amount);
    }
}

// --- Strategy Pattern: SplitStrategy Interface and Implementations ---

interface SplitStrategy {
    List<Transaction> calculateSplits(double totalAmount, User paidBy, List<User> participants, Map<User, Double> splitDetails);
}

class EqualSplitStrategy implements SplitStrategy {
    @Override
    public List<Transaction> calculateSplits(double totalAmount, User paidBy, List<User> participants, Map<User, Double> splitDetails) {
        List<Transaction> transactions = new ArrayList<>();
        if (participants.isEmpty()) {
            return transactions;
        }
        double amountPerPerson = totalAmount / participants.size();
        for (User participant : participants) {
            if (!participant.equals(paidBy)) {
                transactions.add(new Transaction(paidBy, participant, amountPerPerson));
            }
        }
        return transactions;
    }
}

class ExactSplitStrategy implements SplitStrategy {
    @Override
    public List<Transaction> calculateSplits(double totalAmount, User paidBy, List<User> participants, Map<User, Double> splitDetails) {
        List<Transaction> transactions = new ArrayList<>();
        double sumOfExactAmounts = splitDetails.values().stream().mapToDouble(Double::doubleValue).sum();

        // Basic validation: sum of exact amounts must match total amount
        if (Math.abs(sumOfExactAmounts - totalAmount) > 0.01) { // Use a small epsilon for double comparison
            throw new IllegalArgumentException("Sum of exact amounts does not match total expense amount.");
        }

        for (User participant : participants) {
            if (!participant.equals(paidBy)) {
                Double owedAmount = splitDetails.get(participant);
                if (owedAmount != null && owedAmount > 0) {
                    transactions.add(new Transaction(paidBy, participant, owedAmount));
                }
            }
        }
        return transactions;
    }
}

class PercentageSplitStrategy implements SplitStrategy {
    @Override
    public List<Transaction> calculateSplits(double totalAmount, User paidBy, List<User> participants, Map<User, Double> splitDetails) {
        List<Transaction> transactions = new ArrayList<>();
        double sumOfPercentages = splitDetails.values().stream().mapToDouble(Double::doubleValue).sum();

        // Basic validation: sum of percentages must be 100
        if (Math.abs(sumOfPercentages - 100.0) > 0.01) {
            throw new IllegalArgumentException("Sum of percentages must be 100.");
        }

        for (User participant : participants) {
            if (!participant.equals(paidBy)) {
                Double percentage = splitDetails.get(participant);
                if (percentage != null && percentage > 0) {
                    double owedAmount = totalAmount * (percentage / 100.0);
                    transactions.add(new Transaction(paidBy, participant, owedAmount));
                }
            }
        }
        return transactions;
    }
}

class ShareSplitStrategy implements SplitStrategy {
    @Override
    public List<Transaction> calculateSplits(double totalAmount, User paidBy, List<User> participants, Map<User, Double> splitDetails) {
        List<Transaction> transactions = new ArrayList<>();
        double totalShares = splitDetails.values().stream().mapToDouble(Double::doubleValue).sum();

        if (totalShares == 0) {
            throw new IllegalArgumentException("Total shares cannot be zero.");
        }

        double amountPerShare = totalAmount / totalShares;

        for (User participant : participants) {
            if (!participant.equals(paidBy)) {
                Double shares = splitDetails.get(participant);
                if (shares != null && shares > 0) {
                    double owedAmount = amountPerShare * shares;
                    transactions.add(new Transaction(paidBy, participant, owedAmount));
                }
            }
        }
        return transactions;
    }
}

// --- Expense Class ---

class Expense {
    @Getter
    private final String id;
    @Getter
    private final String description;
    @Getter
    private final double amount;
    @Getter
    private final User paidBy;
    private final List<User> participants;
    private final SplitStrategy splitStrategy;
    @Getter
    private final Date date;
    @Getter
    private final String category;
    @Getter
    private final String groupId;// Null if personal expense

    private final Map<User, Double> splitDetails;

    public Expense(String id, String description, double amount, User paidBy, List<User> participants,
                   SplitStrategy splitStrategy, String category, String groupId, Map<User, Double> splitDetails) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.participants = new ArrayList<>(participants); // Defensive copy
        this.splitStrategy = splitStrategy;
        this.date = new Date(); // Timestamp
        this.category = category;
        this.groupId = groupId;
        this.splitDetails = splitDetails;
    }

    public List<User> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    // Delegates to the split strategy to calculate individual transactions
    public List<Transaction> getTransactions() {
        return splitStrategy.calculateSplits(amount, paidBy, participants, splitDetails);
    }

    @Override
    public String toString() {
        return "Expense{" + "id='" + id + '\'' + ", description='" + description + '\'' + ", amount=" + amount + ", paidBy=" + paidBy.getName() + ", participants=" + participants.stream().map(User::getName).collect(Collectors.joining(", ")) + ", splitType=" + splitStrategy.getClass().getSimpleName() + '}';
    }
}

// --- Factory Pattern: ExpenseFactory ---

class ExpenseFactory {
    private static final AtomicLong expenseIdCounter = new AtomicLong(1);

    public static Expense createExpense(String description, double amount, User paidBy, List<User> participants,
                                        SplitType splitType, Map<User, Double> splitDetails, String category, String groupId) {
        String id = "EXP" + expenseIdCounter.getAndIncrement();
        SplitStrategy strategy;

        switch (splitType) {
            case EQUAL:
                strategy = new EqualSplitStrategy();
                break;
            case EXACT:
                strategy = new ExactSplitStrategy();
                break;
            case PERCENTAGE:
                strategy = new PercentageSplitStrategy();
                break;
            case SHARE:
                strategy = new ShareSplitStrategy();
                break;
            default:
                throw new IllegalArgumentException("Unknown split type: " + splitType);
        }
        return new Expense(id, description, amount, paidBy, participants, strategy, category, groupId, splitDetails);
    }
}

// --- Manager Classes ---

class UserManager {
    private static final AtomicLong userIdCounter = new AtomicLong(1);
    private final Map<String, User> users;

    public UserManager() {
        this.users = new ConcurrentHashMap<>();
    }

    public User createUser(String name, String email) {
        String id = "USR" + userIdCounter.getAndIncrement();
        User user = new User(id, name, email);
        users.put(id, user);
        System.out.println("Created User: " + user);
        return user;
    }

    public User getUser(String id) {
        return users.get(id);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }
}

class GroupManager {
    private static final AtomicLong groupIdCounter = new AtomicLong(1);
    private final Map<String, Group> groups;

    public GroupManager() {
        this.groups = new ConcurrentHashMap<>();
    }

    public Group createGroup(String name, List<User> initialMembers) {
        String id = "GRP" + groupIdCounter.getAndIncrement();
        Group group = new Group(id, name, initialMembers);
        groups.put(id, group);
        System.out.println("Created Group: " + group);
        return group;
    }

    public Group getGroup(String id) {
        return groups.get(id);
    }

    public void addMemberToGroup(String groupId, User user) {
        Group group = groups.get(groupId);
        if (group != null) {
            group.addMember(user);
            System.out.println("Added " + user.getName() + " to " + group.getName());
        } else {
            System.out.println("Group with ID " + groupId + " not found.");
        }
    }
}

class BalanceManager {
    // Stores net balances: owedBy -> payer -> amount
    private final Map<User, Map<User, Double>> currentBalances;
    private final NotificationService notificationService;

    public BalanceManager(NotificationService notificationService) {
        this.currentBalances = new ConcurrentHashMap<>();
        this.notificationService = notificationService;
    }

    // Calculates net balances from a list of expenses
    public Map<User, Map<User, Double>> calculateBalances(List<Expense> expenses) {
        Map<User, Map<User, Double>> balances = new ConcurrentHashMap<>();

        for (Expense expense : expenses) {
            // Get the specific split details for this expense
            // In a real app, this would come from the UI or expense creation payload
            // For simulation, we'll assume it's passed or derived.
            // For simplicity in this LLD, we'll assume splitDetails are correctly provided to getTransactions
            // when the expense is processed.
            // A more robust design might have Expense store its raw split data.
            // For now, let's just assume getTransactions can compute without external splitDetails here
            // if the strategy doesn't strictly need it (e.g., EqualSplitStrategy).
            // For Exact/Percentage/Share, the splitDetails would have been part of the Expense creation.
            // So, Expense.getTransactions() should ideally be able to compute based on its internal state.
            // Let's modify Expense.getTransactions to not require splitDetails parameter here.
            // The splitDetails should be encapsulated within the Expense object during its creation.
            // Re-evaluating: The `splitDetails` are specific to the `SplitStrategy` and are needed at the time of `calculateSplits`.
            // The `Expense` object should store the `splitDetails` it was created with, and pass them to its `splitStrategy`.

            // Let's assume Expense stores its splitDetails internally for its strategy.
            // For this LLD, I'll simplify `Expense.getTransactions()` to just call the strategy.
            // The `Expense` class needs to store the `splitDetails` map if its `SplitStrategy` requires it.
            // Let's add `Map<User, Double> rawSplitDetails` to `Expense` and pass it to `getTransactions`.

            // --- REVISED: Expense.getTransactions() will now take the rawSplitDetails from the Expense object itself ---
            // This means Expense needs to store rawSplitDetails.
            // For the LLD, I'll assume `Expense` stores `rawSplitDetails` and passes it to `splitStrategy`.
            // The `Expense` constructor will need `Map<User, Double> rawSplitDetails`.

            // For now, let's assume `Expense.getTransactions()` handles getting the correct splitDetails internally.
            // This is a common point of confusion in Splitwise LLDs.
            // The `SplitStrategy` interface should ideally be designed such that `calculateSplits` takes only `totalAmount`, `paidBy`, `participants`.
            // The `splitDetails` should be part of the `SplitStrategy` *instance* itself, configured at its creation.
            // Let's refine `SplitStrategy` interface to remove `splitDetails` from `calculateSplits` signature.
            // Each concrete strategy will have its own constructor to take necessary details.

            // --- REVISED SplitStrategy and Expense ---
            // The `SplitStrategy` implementations will now hold the `splitDetails` if required.
            // The `ExpenseFactory` will configure the `SplitStrategy` with these details.
            // `Expense.getTransactions()` will simply call `splitStrategy.calculateSplits(amount, paidBy, participants)`.

            List<Transaction> transactions = expense.getTransactions(); // Now, no external splitDetails needed here.

            for (Transaction t : transactions) {
                User owedBy = t.getOwedBy();
                User payer = t.getPayer();
                double amount = t.getAmount();

                // Add to balances: owedBy owes payer
                balances.computeIfAbsent(owedBy, k -> new ConcurrentHashMap<>())
                        .merge(payer, amount, Double::sum);

                // Subtract from balances: payer is owed by owedBy
                balances.computeIfAbsent(payer, k -> new ConcurrentHashMap<>())
                        .merge(owedBy, -amount, Double::sum);
            }
        }
        return balances;
    }


    // Simplifies debts using a basic algorithm (e.g., net settlement)
    public Map<User, Map<User, Double>> simplifyDebts(Map<User, Map<User, Double>> currentBalances) {
        // This is a simplified debt simplification logic.
        // A full implementation would involve a graph algorithm (e.g., finding cycles, min-cost flow).
        // For LLD, we'll just demonstrate the concept of net balances.

        Map<User, Double> netBalances = new ConcurrentHashMap<>(); // Positive = owed, Negative = owes

        // Calculate net balance for each user
        for (Map.Entry<User, Map<User, Double>> entry : currentBalances.entrySet()) {
            User user = entry.getKey();
            double totalNet = 0.0;
            for (Double amount : entry.getValue().values()) {
                totalNet += amount;
            }
            netBalances.put(user, totalNet);
        }

        List<User> creditors = new ArrayList<>(); // Users who are owed money (positive net balance)
        List<User> debtors = new ArrayList<>();   // Users who owe money (negative net balance)

        for (Map.Entry<User, Double> entry : netBalances.entrySet()) {
            if (entry.getValue() > 0.01) { // Creditor
                creditors.add(entry.getKey());
            } else if (entry.getValue() < -0.01) { // Debtor
                debtors.add(entry.getKey());
            }
        }

        Map<User, Map<User, Double>> simplifiedDebts = new ConcurrentHashMap<>();

        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            User creditor = creditors.get(i);
            User debtor = debtors.get(j);

            double owedByCreditor = netBalances.get(creditor); // Positive
            double owedByDebtor = Math.abs(netBalances.get(debtor)); // Positive (amount owed)

            double settlementAmount = Math.min(owedByCreditor, owedByDebtor);

            if (settlementAmount > 0.01) {
                simplifiedDebts.computeIfAbsent(debtor, k -> new ConcurrentHashMap<>())
                        .merge(creditor, settlementAmount, Double::sum);

                netBalances.put(creditor, owedByCreditor - settlementAmount);
                netBalances.put(debtor, -(owedByDebtor - settlementAmount)); // Keep it negative for debtor

                System.out.println(debtor.getName() + " pays " + creditor.getName() + " $" + String.format("%.2f", settlementAmount));
            }

            if (Math.abs(netBalances.get(creditor)) < 0.01) { // Creditor fully settled
                i++;
            }
            if (Math.abs(netBalances.get(debtor)) < 0.01) { // Debtor fully paid
                j++;
            }
        }
        return simplifiedDebts;
    }

    public void recordSettlement(User payer, User receiver, double amount) {
        // In a real system, this would update persistent storage.
        // Here, we just log and notify.
        System.out.println(payer.getName() + " settled $" + String.format("%.2f", amount) + " to " + receiver.getName());
        notificationService.notifyUser(payer, "You settled $" + String.format("%.2f", amount) + " to " + receiver.getName());
        notificationService.notifyUser(receiver, payer.getName() + " settled $" + String.format("%.2f", amount) + " to you.");

        // Update internal balances (simplified)
        currentBalances.computeIfPresent(payer, (k, v) -> {
            v.merge(receiver, -amount, Double::sum); // Payer owes receiver less
            return v;
        });
        currentBalances.computeIfPresent(receiver, (k, v) -> {
            v.merge(payer, amount, Double::sum); // Receiver is owed less by payer
            return v;
        });
    }
}

// --- Observer Pattern: UserObserver Interface and NotificationService ---

interface UserObserver {
    void onNotification(User user, String message);
}

class NotificationService {
    private final List<UserObserver> observers;

    public NotificationService() {
        this.observers = new CopyOnWriteArrayList<>(); // Thread-safe
    }

    public void addObserver(UserObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(UserObserver observer) {
        observers.remove(observer);
    }

    public void notifyUser(User user, String message) {
        for (UserObserver observer : observers) {
            observer.onNotification(user, message);
        }
    }
}

// --- Main Application Class (for simulation) ---

public class SplitwiseAppSimulation {
    public static void main(String[] args) {
        // Initialize Managers and Services
        UserManager userManager = new UserManager();
        GroupManager groupManager = new GroupManager();
        NotificationService notificationService = new NotificationService();
        BalanceManager balanceManager = new BalanceManager(notificationService);

        // Add a simple observer for console logging
        notificationService.addObserver((user, message) ->
                System.out.println("[NOTIFICATION for " + user.getName() + "]: " + message));

        // 1. Create Users
        User alice = userManager.createUser("Alice", "alice@example.com");
        User bob = userManager.createUser("Bob", "bob@example.com");
        User charlie = userManager.createUser("Charlie", "charlie@example.com");
        User david = userManager.createUser("David", "david@example.com");

        // 2. Create a Group
        List<User> tripMembers = Arrays.asList(alice, bob, charlie);
        Group tripGroup = groupManager.createGroup("Weekend Trip", tripMembers);

        // A list to hold all expenses for balance calculation
        List<Expense> allExpenses = new ArrayList<>();

        // 3. Record Expenses

        // Expense 1: Equal Split
        System.out.println("\n--- Expense 1: Dinner (Equal Split) ---");
        // Alice paid $120 for dinner, split equally among Alice, Bob, Charlie
        Map<User, Double> emptySplitDetails = new HashMap<>(); // Not needed for EqualSplitStrategy
        Expense dinner = ExpenseFactory.createExpense("Dinner at Restaurant", 120.0, alice,
                Arrays.asList(alice, bob, charlie), SplitType.EQUAL, emptySplitDetails, "Food", tripGroup.getId());
        allExpenses.add(dinner);
        System.out.println(dinner);
        dinner.getTransactions().forEach(System.out::println);

        // Expense 2: Exact Split
        System.out.println("\n--- Expense 2: Groceries (Exact Split) ---");
        // Bob paid $75 for groceries. Alice owes $20, Charlie owes $30, Bob owes $25 (his part)
        Map<User, Double> grocerySplitDetails = new HashMap<>();
        grocerySplitDetails.put(alice, 20.0);
        grocerySplitDetails.put(bob, 25.0); // Bob's own share
        grocerySplitDetails.put(charlie, 30.0);
        Expense groceries = ExpenseFactory.createExpense("Groceries for BBQ", 75.0, bob,
                Arrays.asList(alice, bob, charlie), SplitType.EXACT, grocerySplitDetails, "Food", tripGroup.getId());
        allExpenses.add(groceries);
        System.out.println(groceries);
        groceries.getTransactions().forEach(System.out::println);

        // Expense 3: Percentage Split
        System.out.println("\n--- Expense 3: Accommodation (Percentage Split) ---");
        // Charlie paid $300 for accommodation. Alice 40%, Bob 30%, Charlie 30%
        Map<User, Double> accommodationSplitDetails = new HashMap<>();
        accommodationSplitDetails.put(alice, 40.0);
        accommodationSplitDetails.put(bob, 30.0);
        accommodationSplitDetails.put(charlie, 30.0);
        Expense accommodation = ExpenseFactory.createExpense("Accommodation", 300.0, charlie,
                Arrays.asList(alice, bob, charlie), SplitType.PERCENTAGE, accommodationSplitDetails, "Stay", tripGroup.getId());
        allExpenses.add(accommodation);
        System.out.println(accommodation);
        accommodation.getTransactions().forEach(System.out::println);

        // Expense 4: Personal Expense (not part of group, but involves Alice and David)
        System.out.println("\n--- Expense 4: Coffee (Personal Expense) ---");
        // Alice paid $15 for coffee. David owes her $15.
        Map<User, Double> coffeeSplitDetails = new HashMap<>();
        coffeeSplitDetails.put(david, 15.0);
        Expense coffee = ExpenseFactory.createExpense("Coffee with David", 15.0, alice,
                Arrays.asList(alice, david), SplitType.EXACT, coffeeSplitDetails, "Beverages", null); // No group ID
        allExpenses.add(coffee);
        System.out.println(coffee);
        coffee.getTransactions().forEach(System.out::println);


        // 4. Calculate Balances
        System.out.println("\n--- Calculating Balances ---");
        Map<User, Map<User, Double>> rawBalances = balanceManager.calculateBalances(allExpenses);
        System.out.println("Raw Balances (Who owes whom):");
        rawBalances.forEach((owedBy, debts) -> {
            debts.forEach((payer, amount) -> {
                if (amount > 0.01) { // Only show positive debts
                    System.out.println(owedBy.getName() + " owes " + payer.getName() + " $" + String.format("%.2f", amount));
                }
            });
        });

        // 5. Simplify Debts
        System.out.println("\n--- Simplifying Debts ---");
        Map<User, Map<User, Double>> simplifiedBalances = balanceManager.simplifyDebts(rawBalances);
        System.out.println("Simplified Debts:");
        simplifiedBalances.forEach((owedBy, debts) -> {
            debts.forEach((payer, amount) -> {
                if (amount > 0.01) { // Only show positive debts
                    System.out.println(owedBy.getName() + " owes " + payer.getName() + " $" + String.format("%.2f", amount));
                }
            });
        });

        // 6. Record Settlement
        System.out.println("\n--- Recording Settlement ---");
        // Let's say Bob pays Alice $50
        balanceManager.recordSettlement(bob, alice, 50.0);

        // Re-calculate balances after settlement (in a real app, this would be more dynamic)
        System.out.println("\n--- Balances After Settlement ---");
        Map<User, Map<User, Double>> updatedBalances = balanceManager.calculateBalances(allExpenses);
        Map<User, Map<User, Double>> updatedSimplifiedBalances = balanceManager.simplifyDebts(updatedBalances);
        updatedSimplifiedBalances.forEach((owedBy, debts) -> {
            debts.forEach((payer, amount) -> {
                if (amount > 0.01) {
                    System.out.println(owedBy.getName() + " owes " + payer.getName() + " $" + String.format("%.2f", amount));
                }
            });
        });
    }
}