package cn.moongn.coworkhub.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import java.util.Date;

@Data
public class UpdateUserVO {

    @NotBlank(message = "真实姓名不能为空")
    @Length(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String phone;

    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "请输入正确的邮箱地址")
    @Length(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    @NotNull(message = "生日不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    @NotNull(message = "性别不能为空")
    // 假设性别使用整数表示：0-未知，1-男，2-女
    @Min(value = 0, message = "性别值不正确")
    @Max(value = 2, message = "性别值不正确")
    private Integer gender;
}
