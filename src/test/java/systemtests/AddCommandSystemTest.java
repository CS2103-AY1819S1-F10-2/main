package systemtests;

import static loanbook.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static loanbook.logic.commands.CommandTestUtil.BIKE_DESC_AMY;
import static loanbook.logic.commands.CommandTestUtil.BIKE_DESC_BOB;
import static loanbook.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static loanbook.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static loanbook.logic.commands.CommandTestUtil.INVALID_BIKE_DESC;
import static loanbook.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static loanbook.logic.commands.CommandTestUtil.INVALID_LOANRATE_DESC;
import static loanbook.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static loanbook.logic.commands.CommandTestUtil.INVALID_NRIC_DESC;
import static loanbook.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static loanbook.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static loanbook.logic.commands.CommandTestUtil.LOANRATE_DESC_AMY;
import static loanbook.logic.commands.CommandTestUtil.LOANRATE_DESC_BOB;
import static loanbook.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static loanbook.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static loanbook.logic.commands.CommandTestUtil.NRIC_DESC_AMY;
import static loanbook.logic.commands.CommandTestUtil.NRIC_DESC_BOB;
import static loanbook.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static loanbook.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static loanbook.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static loanbook.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static loanbook.logic.commands.CommandTestUtil.VALID_NAME_BIKE2;
import static loanbook.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static loanbook.logic.commands.CommandTestUtil.VALID_NRIC_BOB;
import static loanbook.testutil.TypicalLoans.ALICE;
import static loanbook.testutil.TypicalLoans.AMY;
import static loanbook.testutil.TypicalLoans.BOB;
import static loanbook.testutil.TypicalLoans.CARL;
import static loanbook.testutil.TypicalLoans.HOON;
import static loanbook.testutil.TypicalLoans.IDA;
import static loanbook.testutil.TypicalLoans.KEYWORD_MATCHING_MEIER;

import org.junit.Test;

import loanbook.commons.core.Messages;
import loanbook.commons.core.index.Index;
import loanbook.logic.commands.AddCommand;
import loanbook.logic.commands.RedoCommand;
import loanbook.logic.commands.UndoCommand;
import loanbook.model.Model;
import loanbook.model.loan.Email;
import loanbook.model.loan.Loan;
import loanbook.model.loan.LoanId;
import loanbook.model.loan.LoanRate;
import loanbook.model.loan.Name;
import loanbook.model.loan.Nric;
import loanbook.model.loan.Phone;
import loanbook.model.tag.Tag;
import loanbook.testutil.LoanBuilder;
import loanbook.testutil.LoanUtil;

public class AddCommandSystemTest extends LoanBookSystemTest {

    @Test
    public void add() {
        Model model = getModel();

        /* ------------------------ Perform add operations on the shown unfiltered list ----------------------------- */

        /* Case: add a loan without tags to a non-empty loan book, command with leading spaces and trailing spaces
         * -> added
         */
        // Creates a new A
        Loan toAdd = AMY;
        String command = "   " + AddCommand.COMMAND_WORD + "  " + NAME_DESC_AMY + "  " + NRIC_DESC_AMY + " "
                + PHONE_DESC_AMY + " " + EMAIL_DESC_AMY + "   "
                + BIKE_DESC_AMY + "   " + LOANRATE_DESC_AMY + " "
                + TAG_DESC_FRIEND + " ";
        System.out.println("command = " + command);
        assertCommandSuccess(command, toAdd);

        /* Case: undo adding Amy to the list -> Amy deleted */
        command = UndoCommand.COMMAND_WORD;
        String expectedResultMessage = UndoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: redo adding Amy to the list -> Amy added again */
        command = RedoCommand.COMMAND_WORD;
        LoanId expectedLoanId = model.getNextAvailableId();
        Loan toAddWithExpectedLoanId = new LoanBuilder(toAdd)
                .withLoanId(expectedLoanId.toString())
                .build();
        model.addLoan(toAddWithExpectedLoanId);
        expectedResultMessage = RedoCommand.MESSAGE_SUCCESS;
        assertCommandSuccess(command, model, expectedResultMessage);

        /* Case: add a loan with all fields same as another loan in the loan book except name -> added */
        toAdd = new LoanBuilder(AMY).withName(VALID_NAME_BOB).build();
        command = AddCommand.COMMAND_WORD + NAME_DESC_BOB + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY + TAG_DESC_FRIEND;
        assertCommandSuccess(command, toAdd);

        /* Case: add a loan with all fields same as another loan in the loan book except nric -> added */
        toAdd = new LoanBuilder(AMY).withNric(VALID_NRIC_BOB).build();
        command = LoanUtil.getAddCommand(toAdd);
        assertCommandSuccess(command, toAdd);

        /* Case: add a loan with all fields same as another loan in the loan book except bike -> added */
        toAdd = new LoanBuilder(AMY).withBike(VALID_NAME_BIKE2).build();
        command = LoanUtil.getAddCommand(toAdd);
        assertCommandSuccess(command, toAdd);

        /* Case: add to empty loan book -> added */
        deleteAllLoans();
        assertCommandSuccess(ALICE);

        /* Case: add a loan with tags, command with parameters in random order -> added */
        toAdd = BOB;
        command = AddCommand.COMMAND_WORD + TAG_DESC_FRIEND + PHONE_DESC_BOB + NAME_DESC_BOB
                + TAG_DESC_HUSBAND + EMAIL_DESC_BOB + BIKE_DESC_BOB + NRIC_DESC_BOB
                + LOANRATE_DESC_BOB;
        assertCommandSuccess(command, toAdd);

        /* Case: add a loan, missing tags -> added */
        assertCommandSuccess(HOON);

        /* -------------------------- Perform add operation on the shown filtered list ------------------------------ */

        /* Case: filters the loan list before adding -> added */
        showLoansWithName(KEYWORD_MATCHING_MEIER);
        assertCommandSuccess(IDA);

        /* ------------------------ Perform add operation while a loan card is selected --------------------------- */

        /* Case: selects first card in the loan list, add a loan -> added, card selection remains unchanged */
        selectLoan(Index.fromOneBased(1));
        assertCommandSuccess(CARL);

        /* ----------------------------------- Perform invalid add operations --------------------------------------- */

        /* Case: missing name -> rejected */
        command = AddCommand.COMMAND_WORD + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: missing phone -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: missing email -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: missing nric -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: missing bike -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + LOANRATE_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: missing loan rate -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY;
        assertCommandFailure(command, String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        /* Case: invalid keyword -> rejected */
        command = "adds " + LoanUtil.getLoanDetails(toAdd);
        assertCommandFailure(command, Messages.MESSAGE_UNKNOWN_COMMAND);

        /* Case: invalid name -> rejected */
        command = AddCommand.COMMAND_WORD + INVALID_NAME_DESC + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, Name.MESSAGE_NAME_CONSTRAINTS);

        /* Case: invalid nric -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + INVALID_NRIC_DESC + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, Nric.MESSAGE_NRIC_CONSTRAINTS);

        /* Case: invalid phone -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + INVALID_PHONE_DESC + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, Phone.MESSAGE_PHONE_CONSTRAINTS);

        /* Case: invalid email -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY + INVALID_EMAIL_DESC
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY;
        assertCommandFailure(command, Email.MESSAGE_EMAIL_CONSTRAINTS);

        /* Case: invalid bike -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + INVALID_BIKE_DESC + LOANRATE_DESC_AMY;
        assertCommandFailure(command, Name.MESSAGE_NAME_CONSTRAINTS);

        /* Case: invalid rate -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + INVALID_LOANRATE_DESC;
        assertCommandFailure(command, LoanRate.MESSAGE_LOANRATE_CONSTRAINTS);

        /* Case: invalid tag -> rejected */
        command = AddCommand.COMMAND_WORD + NAME_DESC_AMY + NRIC_DESC_AMY + PHONE_DESC_AMY + EMAIL_DESC_AMY
                + BIKE_DESC_AMY + LOANRATE_DESC_AMY + INVALID_TAG_DESC;
        assertCommandFailure(command, Tag.MESSAGE_TAG_CONSTRAINTS);
    }

    /**
     * Executes the {@code AddCommand} that adds {@code toAdd} to the model and asserts that the,<br>
     * 1. Command box displays an empty string.<br>
     * 2. Command box has the default style class.<br>
     * 3. Result display box displays the success message of executing {@code AddCommand} with the details of
     * {@code toAdd}.<br>
     * 4. {@code Storage} and {@code LoanListPanel} equal to the corresponding components in
     * the current model added with {@code toAdd}.<br>
     * 5. Browser url and selected card remain unchanged.<br>
     * 6. Status bar's sync status changes.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code LoanBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see LoanBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandSuccess(Loan toAdd) {
        assertCommandSuccess(LoanUtil.getAddCommand(toAdd), toAdd);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(Loan)}. Executes {@code command}
     * instead.
     * @see AddCommandSystemTest#assertCommandSuccess(Loan)
     */
    private void assertCommandSuccess(String command, Loan toAdd) {
        Model expectedModel = getModel();

        LoanId expectedLoanId = expectedModel.getNextAvailableId();
        Loan expectedLoanToAdd = new LoanBuilder(toAdd)
                .withLoanId(expectedLoanId.toString())
                .withLoanStatus("ONGOING")
                .build();

        expectedModel.addLoan(expectedLoanToAdd);
        String expectedResultMessage = String.format(AddCommand.MESSAGE_SUCCESS, expectedLoanToAdd);

        assertCommandSuccess(command, expectedModel, expectedResultMessage);
    }

    /**
     * Performs the same verification as {@code assertCommandSuccess(String, Loan)} except asserts that
     * the,<br>
     * 1. Result display box displays {@code expectedResultMessage}.<br>
     * 2. {@code Storage} and {@code LoanListPanel} equal to the corresponding components in
     * {@code expectedModel}.<br>
     * @see AddCommandSystemTest#assertCommandSuccess(String, Loan)
     */
    private void assertCommandSuccess(String command, Model expectedModel, String expectedResultMessage) {
        executeCommand(command);
        assertApplicationDisplaysExpected("", expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsDefaultStyle();
        assertStatusBarUnchangedExceptSyncStatus();
    }

    /**
     * Executes {@code command} and asserts that the,<br>
     * 1. Command box displays {@code command}.<br>
     * 2. Command box has the error style class.<br>
     * 3. Result display box displays {@code expectedResultMessage}.<br>
     * 4. {@code Storage} and {@code LoanListPanel} remain unchanged.<br>
     * 5. Browser url, selected card and status bar remain unchanged.<br>
     * Verifications 1, 3 and 4 are performed by
     * {@code LoanBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)}.<br>
     * @see LoanBookSystemTest#assertApplicationDisplaysExpected(String, String, Model)
     */
    private void assertCommandFailure(String command, String expectedResultMessage) {
        Model expectedModel = getModel();

        executeCommand(command);
        assertApplicationDisplaysExpected(command, expectedResultMessage, expectedModel);
        assertSelectedCardUnchanged();
        assertCommandBoxShowsErrorStyle();
        assertStatusBarUnchanged();
    }
}
