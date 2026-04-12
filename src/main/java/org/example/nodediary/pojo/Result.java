package org.example.nodediary.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Result {
    private Integer code;//响应码
    private String message;//响应信息 描述字符串
    private Object data;//返回的数据

    public static Result success(String message) {
        return new Result(200, message, null);
    }

    public static Result success(String message, Object data) {

        return new Result(200, message, data);
    }

    public static Result error(String message) {
        return new Result(400, message, null);
    }
}
