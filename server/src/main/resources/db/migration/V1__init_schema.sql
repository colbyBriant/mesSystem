-- 创建用户表（users）及其索引
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY COMMENT '用户ID，格式为U开头的字符串',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名，唯一',
    password VARCHAR(255) NOT NULL COMMENT '密码，加密存储',
    name VARCHAR(100) NOT NULL COMMENT '用户真实姓名',
    email VARCHAR(100) COMMENT '电子邮箱',
    phone VARCHAR(20) COMMENT '手机号码',
    department VARCHAR(100) COMMENT '所属部门',
    role VARCHAR(50) NOT NULL COMMENT '用户角色，如ADMIN、DOCTOR等',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态，如ACTIVE、INACTIVE',
    last_login_time TIMESTAMP COMMENT '最后登录时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '用户信息表';

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- 创建患者表（patients）及其索引
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(36) PRIMARY KEY COMMENT '患者ID，UUID格式',
    medical_number VARCHAR(50) UNIQUE NOT NULL COMMENT '就诊号，唯一',
    name VARCHAR(100) NOT NULL COMMENT '患者姓名',
    gender VARCHAR(10) COMMENT '性别',
    age INT COMMENT '年龄',
    id_card VARCHAR(20) UNIQUE COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    address VARCHAR(200) COMMENT '住址',
    card_number VARCHAR(50) COMMENT '医保卡号',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '患者信息表';

CREATE INDEX idx_patients_medical_number ON patients(medical_number);
CREATE INDEX idx_patients_name ON patients(name);
CREATE INDEX idx_patients_id_card ON patients(id_card);
CREATE INDEX idx_patients_card_number ON patients(card_number);

-- 创建分析报告表（analysis_reports）及其索引
CREATE TABLE IF NOT EXISTS analysis_reports (
    id VARCHAR(36) PRIMARY KEY COMMENT '报告ID，UUID格式',
    patient_id VARCHAR(36) NOT NULL COMMENT '患者ID，关联patients表',
    report_type VARCHAR(50) NOT NULL COMMENT '报告类型，如TONGUE、FACE、PULSE等',
    original_image LONGBLOB COMMENT '原始图像数据',
    analysis_image LONGBLOB COMMENT '分析后的图像数据',
    analysis_result TEXT COMMENT '分析结果，JSON格式存储详细信息',
    constitution_result TEXT COMMENT '体质辨识结果，JSON格式存储详细信息',
    main_constitution_type VARCHAR(50) COMMENT '主要体质类型',
    secondary_constitution_type VARCHAR(50) COMMENT '次要体质类型',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
) COMMENT '患者分析报告表';

CREATE INDEX idx_analysis_reports_patient_id ON analysis_reports(patient_id);
CREATE INDEX idx_analysis_reports_report_type ON analysis_reports(report_type);
CREATE INDEX idx_analysis_reports_create_time ON analysis_reports(create_time);
CREATE INDEX idx_analysis_reports_main_constitution_type ON analysis_reports(main_constitution_type);

-- 创建系统日志表（system_logs）及其索引
CREATE TABLE IF NOT EXISTS system_logs (
    id VARCHAR(36) PRIMARY KEY COMMENT '日志ID，UUID格式',
    user_id VARCHAR(36) COMMENT '操作用户ID，关联users表',
    action VARCHAR(100) NOT NULL COMMENT '操作类型',
    description TEXT COMMENT '操作详情',
    ip_address VARCHAR(45) COMMENT '操作IP地址',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) COMMENT '系统操作日志表';

CREATE INDEX idx_system_logs_user_id ON system_logs(user_id);
CREATE INDEX idx_system_logs_action ON system_logs(action);
CREATE INDEX idx_system_logs_create_time ON system_logs(create_time);

-- 创建系统配置表（system_config）及其索引
CREATE TABLE IF NOT EXISTS system_config (
    id VARCHAR(36) PRIMARY KEY COMMENT '配置ID，UUID格式',
    config_key VARCHAR(100) UNIQUE NOT NULL COMMENT '配置键名',
    config_value TEXT NOT NULL COMMENT '配置值',
    description VARCHAR(200) COMMENT '配置说明',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '系统配置表';

CREATE INDEX idx_system_config_key ON system_config(config_key);

-- 创建消息表（messages）及其索引
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY COMMENT '消息ID，UUID格式',
    sender_id VARCHAR(36) NOT NULL COMMENT '发送者ID，关联users表',
    receiver_id VARCHAR(36) NOT NULL COMMENT '接收者ID，关联users表',
    content TEXT NOT NULL COMMENT '消息内容',
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD' COMMENT '消息状态，如UNREAD、READ',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
) COMMENT '用户消息表';

CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_create_time ON messages(create_time);

-- 初始化系统管理员账户
INSERT INTO users (id, username, password, name, role, status, create_time, update_time)
VALUES ('U001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'ADMIN', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = NOW();

-- 初始化系统配置
INSERT INTO system_config (id, config_key, config_value, description, create_time, update_time)
VALUES 
(UUID(), 'SYSTEM_NAME', '中医体质辨识系统', '系统名称', NOW(), NOW()),
(UUID(), 'SYSTEM_VERSION', '1.1.0', '系统版本', NOW(), NOW()),
(UUID(), 'MEDICAL_NUMBER_PREFIX', 'MN', '就诊号前缀', NOW(), NOW())
ON DUPLICATE KEY UPDATE update_time = NOW();
