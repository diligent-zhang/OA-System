package com.tianyu.oasystem.mapper;

import com.tianyu.oasystem.dto.NoticeQueryDTO;
import com.tianyu.oasystem.entity.SysNotice;
import com.tianyu.oasystem.vo.NoticeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SysNoticeMapper {

    /** 管理端分页：全部状态 */
    List<NoticeVO> selectPageAdmin(NoticeQueryDTO query);

    /** 员工端分页：仅已发布，置顶+时间倒序 */
    List<NoticeVO> selectPagePublished(NoticeQueryDTO query);

    NoticeVO selectVOById(@Param("noticeId") Long noticeId);

    SysNotice selectById(@Param("noticeId") Long noticeId);

    List<NoticeVO> selectLatest(@Param("limit") int limit);

    int countAll();

    int insert(SysNotice notice);

    int update(SysNotice notice);

    int updateStatus(@Param("noticeId") Long noticeId, @Param("status") Integer status);

    int deleteById(@Param("noticeId") Long noticeId);

    /** 记录已读（幂等） */
    int insertRead(@Param("noticeId") Long noticeId, @Param("userId") Long userId);

    int countRead(@Param("noticeId") Long noticeId);

    /** 已读用户账号列表 */
    List<String> selectReadUsernames(@Param("noticeId") Long noticeId);

    /** 当前用户已读的公告ID集合（批量判断列表已读） */
    List<Long> selectReadNoticeIds(@Param("userId") Long userId,
                                   @Param("noticeIds") List<Long> noticeIds);

    int deleteReads(@Param("noticeId") Long noticeId);
}
