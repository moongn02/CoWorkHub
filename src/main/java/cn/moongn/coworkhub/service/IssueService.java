package cn.moongn.coworkhub.service;

import cn.moongn.coworkhub.model.Issue;
import cn.moongn.coworkhub.model.dto.IssueDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface IssueService extends IService<Issue> {

    /**
     * 创建问题
     * @param issue 问题对象
     * @return 是否成功
     */
    boolean createIssue(Issue issue);

    /**
     * 将Issue转换为IssueDTO
     * @param issue 问题对象
     * @return IssueDTO
     */
    IssueDTO convertToDTO(Issue issue);

    /**
     * 分页查询问题
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param params 查询参数
     * @return 分页结果
     */
    Page<IssueDTO> pageIssues(Integer pageNum, Integer pageSize, Map<String, Object> params);
}