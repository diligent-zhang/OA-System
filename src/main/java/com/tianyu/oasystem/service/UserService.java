package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.PasswordDTO;
import com.tianyu.oasystem.dto.ProfileDTO;
import com.tianyu.oasystem.vo.ProfileVO;

public interface UserService {

    ProfileVO myProfile();

    void updateProfile(ProfileDTO dto);

    void updatePassword(PasswordDTO dto);
}
