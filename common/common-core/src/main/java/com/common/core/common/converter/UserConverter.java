package com.common.core.common.converter;

import com.common.core.model.user.entity.User;
import com.common.core.model.user.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserConverter {

    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    /**
     * UserVO 转 User
     */
    User toEntity(UserVO userVO);

    /**
     * User 转 UserVO
     */
    UserVO toVO(User user);
}
