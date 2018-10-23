package seedu.address.model.loan;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a LoanStatus in the LoanBook.
 */
public enum LoanStatus {
    ONGOING {
        public String toString() {
            return "Ongoing";
        }
    },
    RETURNED {
        public String toString() {
            return "Returned";
        }
    },
    DELETED {
        public String toString() {
            return "Deleted";
        }
    };

    private static final Set<String> values = new HashSet<String>(LoanStatus.values().length);

    public static final String MESSAGE_LOANSTATUS_CONSTRAINTS =
            "LoanStatuses can only take values 'ONGOING', 'RETURNED', or 'DELETED'";

    static {
        for (LoanStatus ls : LoanStatus.values()) {
            values.add(ls.name());
        }
    }

    public static boolean isValidLoanStatus(String string) {
        return values.contains(string);
    }
}