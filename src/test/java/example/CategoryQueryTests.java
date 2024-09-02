package example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Rollback
@Sql(statements = {
        """
        insert into category (category_id, category_name)
            values
                (1, '数学'),
                (2, '高校数学'),
                (3, '中学数学'),
                (4, '磁性流体'),
                (5, 'ベクトル'),
                (6, '分数'),
                (7, '英語'),
                (8, '高校英語'),
                (9, '中学英語')
        ;
        """,
        """
        insert into category_path (ancestor, descendant)
            values
                (1, 1),
                (1, 2),
                (1, 3),
                (1, 4),
                (1, 5),
                (1, 6),
                (2, 4),
                (2, 5),
                (3, 6),
                (7, 7),
                (7, 8),
                (7, 9)
        ;
        """
})
public class CategoryQueryTests {

    private final JdbcClient jdbc;

    @Autowired
    CategoryQueryTests(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Test
    void ある階層より下の階層をすべて参照する() {
        var categoryId = 2L;
        var sql = """
                select category.category_id,
                       category.category_name,
                       category_path.ancestor,
                       category_path.descendant
                  from category
                  join category_path
                    on category.category_id = category_path.descendant
                 where category_path.ancestor = ?
                """;
        var result = jdbc.sql(sql)
                .param(categoryId)
                .query(Category.class)
                .list();
        Assertions.assertEquals(new Category(4, "磁性流体"), result.get(0));
        Assertions.assertEquals(new Category(5, "ベクトル"), result.get(1));
    }

    @Test
    void ある階層から見たときの最下層をすべて参照する() {
        var categoryId = 1L;
        var sql = """
                select category.category_id,
                       category.category_name,
                       desendant_path.ancestor,
                       desendant_path.descendant
                  from category
                  left join category_path as desendant_path
                         on category.category_id = desendant_path.descendant
                  left join category_path as ancestor_path
                         on category.category_id = ancestor_path.ancestor
                 where desendant_path.ancestor = ?
                   and ancestor_path.ancestor is null
                """;
        var result = jdbc.sql(sql)
                .param(categoryId)
                .query(Category.class)
                .list();
        Assertions.assertEquals(new Category(4, "磁性流体"), result.get(0));
        Assertions.assertEquals(new Category(5, "ベクトル"), result.get(1));
        Assertions.assertEquals(new Category(6, "分数"), result.get(2));
    }

    @Test
    void ある階層の一つ上の階層を参照する() {
        var categoryId = 2L;
        var sql = """
                select category.category_id,
                       category.category_name,
                       category_path.ancestor,
                       category_path.descendant
                  from category
                  left join category_path
                         on category.category_id = category_path.ancestor
                 where category_path.descendant = ?
                """;
        var result = jdbc.sql(sql)
                .param(categoryId)
                .query(Category.class)
                .single();
        Assertions.assertEquals(new Category(1, "数学"), result);
    }
}
