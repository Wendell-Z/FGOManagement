package com.fgo.management.mapper;

import com.fgo.management.model.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {


    UserAccount queryUserByUserAccount(@Param("userAccount") UserAccount userAccount);
}
