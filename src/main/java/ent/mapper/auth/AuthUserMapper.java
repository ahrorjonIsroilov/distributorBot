package ent.mapper.auth;


import ent.dto.auth.AuthUserCreateDto;
import ent.dto.auth.AuthUserDto;
import ent.dto.auth.AuthUserUpdateDto;
import ent.entity.auth.AuthUser;
import ent.mapper.BaseMapper;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface AuthUserMapper extends BaseMapper<AuthUser, AuthUserDto, AuthUserCreateDto, AuthUserUpdateDto> {
    @Override
    AuthUserDto toDto(AuthUser user);

    @Override
    List<AuthUserDto> toDto(List<AuthUser> e);

    @Override
    AuthUser fromCreateDto(AuthUserCreateDto authUserCreateDto);

    @Override
    AuthUser fromUpdateDto(AuthUserUpdateDto d);
}
