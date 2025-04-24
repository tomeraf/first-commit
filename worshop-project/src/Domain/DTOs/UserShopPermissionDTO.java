package Domain.DTOs;

import Domain.Permission;

public class UserShopPermissionDTO {
    public int userId;
    public int shopId;
    public Permission permission; // could also be an enum or serialized string
}
