package ent.validator;

import ent.dto.Dto;
import ent.dto.GenericDto;

import javax.xml.bind.ValidationException;
import java.io.Serializable;

public abstract class AbstractValidator<
        CD extends Dto,
        UD extends GenericDto,
        K extends Serializable> implements Validator {

    public abstract void validateKey(K id) throws ValidationException;

    public abstract void validateOnCreate(CD cd) throws ValidationException;

    public abstract void validateOnUpdate(UD ud) throws ValidationException;

}
