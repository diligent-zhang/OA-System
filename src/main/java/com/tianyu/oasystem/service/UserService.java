package com.tianyu.oasystem.service;

import com.tianyu.oasystem.dto.PasswordDTO;
import com.tianyu.oasystem.dto.ProfileDTO;
import com.tianyu.oasystem.vo.ProfileVO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    ProfileVO myProfile();

    void updateProfile(ProfileDTO dto);

    void updatePassword(PasswordDTO dto);

    /** 上传并更新当前用户头像 */
    String uploadAvatar(MultipartFile file);
}
