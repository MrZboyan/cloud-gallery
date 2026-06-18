# 数据库初始化
drop schema if exists picture_cloud;

create database if not exists picture_cloud;

use picture_cloud;

-- 用户表
create table user
(
    userId       bigint auto_increment comment 'userId' primary key,
    userAccount  varchar(256)                          not null comment '账号',
    userPassword varchar(512)                          not null comment '密码',
    userName     varchar(256)                          null comment '用户昵称',
    userAvatar   varchar(1024)                         null comment '用户头像',
    userProfile  varchar(512)                          null comment '用户简介',
    userRole     varchar(32) default 'user'            not null comment '用户角色：user/admin/ban/vip',
    createTime   datetime    default current_timestamp not null comment '创建时间',
    updateTime   datetime    default current_timestamp not null on update current_timestamp comment '更新时间',
    isDelete     tinyint     default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图片表
create table picture
(
    pictureId     bigint auto_increment comment 'pictureId' primary key,
    galleryId     bigint                             not null comment '图库 id',
    userId        bigint                             not null comment '创建用户 id',
    url           varchar(512)                       not null comment '图片 url',
    thumbnailUrl  varchar(512)                       null comment '缩略图 url',
    name          varchar(64)                        not null comment '图片名称',
    introduction  varchar(128)                       null comment '简介',
    category      varchar(64)                        null comment '分类（JSON 数组）',
    tags          varchar(512)                       null comment '标签（JSON 数组）',
    picSize       bigint                             null comment '图片大小（字节）',
    picWidth      int                                null comment '图片宽度',
    picHeight     int                                null comment '图片高度',
    picScale      double                             null comment '图片宽高比例',
    picFormat     varchar(16)                        null comment '图片格式',
    picColor      varchar(16)                        null comment '图片主色调',
    reviewStatus  tinyint  default 0                 not null comment '审核状态：0-待审核; 1-通过; 2-拒绝',
    reviewMessage varchar(512)                       null comment '审核信息',
    reviewerId    bigint                             null comment '审核人 ID',
    reviewTime    datetime                           null comment '审核时间',
    createTime    datetime default current_timestamp not null comment '创建时间',
    updateTime    datetime default current_timestamp not null on update current_timestamp comment '更新时间',
    isDelete      tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId),             -- 提升基于用户 ID 的查询性能
    INDEX idx_galleryId (galleryId)
) comment '图片' collate = utf8mb4_unicode_ci;

-- 团队表 硬删除
create table team
(
    teamId     bigint auto_increment comment 'teamId' primary key,
    ownerId    bigint                             not null comment '团队创建者',
    createTime datetime default current_timestamp not null comment '创建时间',
    index idx_ownerId (ownerId)
) comment '团队' collate = utf8mb4_unicode_ci;

-- 团队成员表 硬删除
create table team_member
(
    teamMemberId bigint auto_increment comment 'teamMemberId' primary key,
    teamId       bigint                                not null comment '团队 id',
    userId       bigint                                not null comment '用户 id',
    role         varchar(32) default 'member'          not null comment '角色：admin/editor/member',
    createTime   datetime    default current_timestamp not null comment '创建时间',
    index idx_teamId (teamId),
    index idx_userId (userId),
    unique key uk_teamId_userId (teamId, userId)
) comment '团队成员' collate = utf8mb4_unicode_ci;

-- 图库表
create table gallery
(
    galleryId    bigint auto_increment comment 'galleryId' primary key,
    name         varchar(128)                          not null comment '图库名称',
    introduction varchar(256)                          null comment '图库简介',
    type         varchar(32)                           not null comment '图库类型 COMMON-公共 SHARE-共享 TEAM-团队 PRIVATE-私人',
    level        varchar(32) default 'default'         not null comment '等级 default-默认 mid-中级 high-高级 super-至尊（仅为公共图库享有）',
    usedCapacity bigint      default 0                 null comment '已使用容量',
    capacity     bigint      default 5368709120        null comment '最大容量（字节）默认 5 GB',
    ownerId      bigint                                not null comment '图库归属者',
    teamId       bigint                                null comment '图库归属团队（仅当图库类型为团队时不为空）',
    createTime   datetime    default current_timestamp not null comment '创建时间',
    isDelete     tinyint     default 0                 not null comment '是否删除',
    index idx_ownerId (ownerId),
    index idx_teamId (teamId)
) comment '图库' collate = utf8mb4_unicode_ci;

-- 用户权限快照表
create table user_permission_snapshot
(
    primaryId       bigint auto_increment primary key,
    userId          bigint                                 not null,
    galleryId       bigint                                 not null,
    galleryType     varchar(16)                            not null comment '0-public,1-shared,2-team,3-private',
    role            varchar(16)                            not null comment 'OWNER/EDITOR/VIEWER/TEAM_MEMBER/ADMIN',
    teamId          bigint                                 null,
    permBits        int          default 0                 not null comment '权限位掩码', -- 使用整数位掩码表示权限位，便于扩展和索引
    lastRefreshTime datetime     default current_timestamp not null,                      -- 最近一次权限快照生成时间
    eventVersion    bigint       default 0                 not null,                      -- 用于幂等/版本控制，保证事件顺序更新
    source          varchar(128) default 'event'           not null,                      -- 记录来源与操作人，便于回溯
    unique key uk_user_gallery (userId, galleryId),
    key idx_galleryId (galleryId),
    key idx_userId (userId),
    key idx_teamId (teamId),
    key idx_eventVersion (galleryId, eventVersion)
) comment '用户权限快照表' collate = utf8mb4_unicode_ci;

-- 团队聊天室表
create table team_chat_room
(
    roomId     bigint auto_increment comment '聊天室ID' primary key,
    teamId     bigint                             not null comment '团队ID',
    name       varchar(128)                       null comment '聊天室名称（默认与团队名一致）',
    createTime datetime default current_timestamp not null comment '创建时间',
    updateTime datetime default current_timestamp not null on update current_timestamp comment '更新时间',
    unique key uk_teamId (teamId)
) comment '团队聊天室' collate = utf8mb4_unicode_ci;

-- 团队聊天消息表
create table team_chat_message
(
    messageId    bigint auto_increment comment '消息ID' primary key,
    roomId       bigint                                not null comment '聊天室ID',
    senderId     bigint                                not null comment '发送者用户ID',
    messageType  varchar(32) default 'TEXT'            not null comment '消息类型：TEXT/IMAGE',
    content      text                                  not null comment '消息内容（纯文本或图片URL等）',
    refMessageId bigint                                null comment '引用的消息ID',
    createTime   datetime    default current_timestamp not null comment '发送时间',
    isDelete     tinyint     default 0                 not null comment '是否删除',
    index idx_roomId (roomId),
    index idx_senderId (senderId)
) comment '团队聊天消息' collate = utf8mb4_unicode_ci;

-- 团队聊天消息阅读状态表
create table team_chat_message_read
(
    primaryId bigint auto_increment comment '主键ID' primary key,
    messageId bigint                             not null comment '消息ID',
    userId    bigint                             not null comment '用户ID',
    readTime  datetime default current_timestamp not null comment '读取时间',
    unique key uk_message_user (messageId, userId)
) comment '消息阅读状态' collate = utf8mb4_unicode_ci;

