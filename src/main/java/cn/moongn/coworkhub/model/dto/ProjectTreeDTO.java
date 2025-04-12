package cn.moongn.coworkhub.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProjectTreeDTO {
    private Long id;
    private String name;
    private Long parentId;
    private List<ProjectTreeDTO> children;
}