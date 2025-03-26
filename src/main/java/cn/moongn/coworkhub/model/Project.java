package cn.moongn.coworkhub.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;             // 项目ID
    private String name;         // 项目名称
    private Long parentId;       // 上级项目ID
    private Long departmentId;   // 所属部门ID
    private Long updaterId;      // 项目最后更新人ID
    private Integer status;      // 项目状态（0：禁用, 1：启用）
}