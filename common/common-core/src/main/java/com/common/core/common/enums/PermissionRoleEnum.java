package com.common.core.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum PermissionRoleEnum {
    // 普通图库角色
    OWNER("拥有者", "owner"),
    // 团队角色
    TEAM_MEMBER("团队成员", "team_member"),
    TEAM_EDITOR("团队编辑", "team_editor"),
    TEAM_ADMIN("团队管理员", "team_admin");

    private final String text;

    private final String value;

    PermissionRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 获取值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    /**
     * 根据 value 获取枚举
     */
    public static PermissionRoleEnum getEnumByValue(String value) {
        if (value == null) {
            return null;
        }
        for (PermissionRoleEnum anEnum : PermissionRoleEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
