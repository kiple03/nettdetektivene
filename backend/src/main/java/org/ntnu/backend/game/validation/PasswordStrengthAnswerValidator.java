package org.ntnu.backend.game.validation;

import java.util.List;

import org.ntnu.backend.game.dto.SubmitAnswerRequest;
import org.ntnu.backend.game.dto.subtypes.PasswordStrengthAnswer;
import org.ntnu.backend.game.model.Option;
import org.ntnu.backend.game.model.PasswordStrengthOption;
import org.ntnu.backend.game.model.enums.LevelType;
import org.springframework.stereotype.Component;

/**
 * Validates password-strength answers by checking the submitted password against
 * the configured rules (min length, digit, upper/lower case, symbol requirements).
 */
@Component
public class PasswordStrengthAnswerValidator implements SubmittedAnswerValidator{

    @Override
    public LevelType supportedType(){
        return LevelType.PASSWORD_STRENGTH;
    }

    /**
     * @param options   single-element list containing the {@link PasswordStrengthOption} rule set
     * @param submission must be a {@link PasswordStrengthAnswer}; throws {@link IllegalArgumentException} otherwise
     * @return successful result if all rules pass; failure result listing missing criteria in Norwegian
     */
    @Override
    public ValidationResult validate(List<Option> options, SubmitAnswerRequest submission){
        if(!(submission instanceof PasswordStrengthAnswer answer)){
            throw new IllegalArgumentException("PASSWORD_STRENGTH requires PasswordStrengthAnswer, got " + submission.getClass().getSimpleName());
        }
        PasswordStrengthOption rule = (PasswordStrengthOption) options.get(0);
        String pwd = answer.getPassword() == null ? "" : answer.getPassword();

        StringBuilder missing = new StringBuilder();
        if(pwd.length() < rule.getMinLength()) 
            missing.append("Minst ").append(rule.getMinLength()).append(" tegn\n");
        if(rule.isRequireDigit() && pwd.chars().noneMatch(Character::isDigit))
            missing.append("Minst ett tall\n");
        if(rule.isRequireUpper() && pwd.chars().noneMatch(Character::isUpperCase))
            missing.append("Minst èn stor bokstav\n");
        if(rule.isRequireLower() && pwd.chars().noneMatch(Character::isLowerCase))
            missing.append("Minst èn liten bokstav\n");
        if(rule.isRequireSymbol() && pwd.chars().noneMatch(c -> !Character.isLetterOrDigit(c)))
            missing.append("Minst ett symbol\n");

        if(missing.length() == 0){
            return new ValidationResult(true, rule.getSuccessFeedback(), List.of(), List.of(), List.of());
        } return new ValidationResult(false, rule.getFailureFeedback() + "\nMangler:\n"+ missing, List.of(), List.of(), List.of());
    }
}
