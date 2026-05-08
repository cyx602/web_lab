-- 1. 关闭外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 【关键修改】初次运行请务必注释掉下面这些 TRUNCATE 语句！
-- 等你成功启动一次、表都建好以后，再把注释打开。
-- TRUNCATE TABLE borrow_records;
-- TRUNCATE TABLE reservations;
-- TRUNCATE TABLE reviews;
-- TRUNCATE TABLE notifications;
-- TRUNCATE TABLE users;
-- TRUNCATE TABLE books;

-- 3. 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 4. 插入测试用户 (使用 REPLACE 防止重复启动报错)
REPLACE INTO users (id, student_id, password, name, phone, card_status) VALUES
                                                                            (1, '20210001', '123456', '张三', '13811112222', '正常'),
                                                                            (2, '20210002', '123456', '李四', '13933334444', '正常'),
                                                                            (3, '20210003', '123456', '王五', '13755556666', '正常'),
                                                                            (4, '20210004', '123456', '赵六', '13677778888', '挂失');

-- 5. 插入测试图书
REPLACE INTO books (id, isbn, title, author, category, total, available) VALUES
                                                                             (1, '9787111544296', 'Java核心技术 卷I', 'Cay S. Horstmann', '计算机', 5, 5),
                                                                             (2, '9787121362217', '深入理解Java虚拟机', '周志明', '计算机', 3, 3),
                                                                             (3, '9787302423287', '数据结构', '严蔚敏', '教材', 10, 10),
                                                                             (4, '9787506365437', '白夜行', '东野圭吾', '文学', 2, 2),
                                                                             (5, '9787544270878', '解忧杂货店', '东野圭吾', '文学', 4, 4),
                                                                             (6, '9787020125265', '活着', '余华', '文学', 6, 0),
                                                                             (7, '9787532731077', '挪威的森林', '村上春树', '文学', 3, 3),
                                                                             (8, '9787111213826', '算法导论', 'Thomas H. Cormen', '计算机', 2, 2),
                                                                             (9, '9787115277466', 'JavaScript高级程序设计', 'Nicholas C. Zakas', '计算机', 5, 5),
                                                                             (10, '9787020111299', '三体', '刘慈欣', '科幻', 8, 8);