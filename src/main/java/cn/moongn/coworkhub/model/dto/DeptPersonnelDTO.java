package cn.moongn.coworkhub.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class DeptPersonnelDTO {
    private Long id;
    private String label;
    private List<DeptPersonnelDTO> children;
    private Boolean isLeaf; // 是否为叶子节点（人员）
}