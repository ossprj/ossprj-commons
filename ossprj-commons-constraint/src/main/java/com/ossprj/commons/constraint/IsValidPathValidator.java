package com.ossprj.commons.constraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.File;
import java.nio.file.Path;

public class IsValidPathValidator implements ConstraintValidator<IsValidPath, Path> {

    private boolean isDirectory = false;
    private boolean isFile = false;

    private boolean isReadable = false;
    private boolean isWriteable = false;

    @Override
    public void initialize(IsValidPath constraintAnnotation) {
        isDirectory = constraintAnnotation.isDirectory();
        isFile = constraintAnnotation.isFile();

        isReadable = constraintAnnotation.isReadable();
        isWriteable = constraintAnnotation.isWriteable();
    }

    @Override
    public boolean isValid(Path path, ConstraintValidatorContext context) {
        // Ignore null Path(s), should be enforced separately via NotNull
        if (path == null) {
            return true;
        }

        final File pathAsFile = path.toFile();

        // Ensure the Path exists
        if (!pathAsFile.exists()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path must exist").addConstraintViolation();
            return false;
        }

        // Should it be a directory ?
        if (isDirectory && !pathAsFile.isDirectory()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path must be a directory").addConstraintViolation();
            return false;
        }

        // Should it be a file ?
        if (isFile && !pathAsFile.isFile()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path must be a file").addConstraintViolation();
            return false;
        }

        // Is it readable ?
        if (isReadable && !pathAsFile.canRead()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path must be readable").addConstraintViolation();
            return false;
        }

        // Is it writeable ?
        if (isWriteable && !pathAsFile.canWrite()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Path must be writeable").addConstraintViolation();
            return false;
        }

        return true;
    }
}
