package loanbook.logic.commands;

import static java.util.Objects.requireNonNull;
import static loanbook.commons.util.CollectionUtil.requireAllNonNull;
import static loanbook.logic.parser.CliSyntax.PREFIX_EMAIL;
import static loanbook.logic.parser.CliSyntax.PREFIX_PASSWORD;

import loanbook.commons.core.Messages;
import loanbook.logic.CommandHistory;
import loanbook.logic.commands.exceptions.CommandException;
import loanbook.model.Model;
import loanbook.model.loan.Email;

/**
 * Set user's email to the app.
 */
public class SetEmailCommand extends PasswordProtectedCommand {

    public static final String COMMAND_WORD = "setemail";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Set your email to the app. "
            + "Parameter: e/NEWEMAIL x/PASSWORDFORAPP\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_EMAIL + "my.email@gmail.com "
            + PREFIX_PASSWORD + "a12345";

    public static final String MESSAGE_SUCCESS = "Your email is set successfully!";

    private final Email newEmail;
    private final String password;

    /**
     * Creates an SetEmailCommand to set user's {@code Email} according to the {@code newEmail} provided.
     */
    public SetEmailCommand(Email newEmail, String password) {
        super(password, COMMAND_WORD);
        requireAllNonNull(newEmail, password);
        this.newEmail = newEmail;
        this.password = password;
    }

    @Override
    public CommandResult execute(Model model, CommandHistory history) throws CommandException {
        requireNonNull(model);
        assertCorrectPassword(model);

        if ((newEmail.value).equals(model.getMyEmail())) {
            throw new CommandException(Messages.MESSAGE_SAME_AS_OLDEMAIL);
        }

        if (!Email.isValidGmail(newEmail.value)) {
            throw new CommandException(Messages.MESSAGE_INVALID_EMAIL);
        }

        model.setMyEmail(newEmail.value);

        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof SetEmailCommand // instanceof handles nulls
                && newEmail.equals(((SetEmailCommand) other).newEmail)
                && password.equals(((SetEmailCommand) other).password)); // state check
    }
}
