import java.sql.*;

/**
 * Created by tttppp606 on 2019/1/28.
 */
public class demo {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/mmall_learning?characterEncoding=utf-8", "root", "1234");
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM mmall_product WHERE id = 27");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()){
            String create_time = resultSet.getString("create_time");
            System.out.println(create_time);

        }


    }
}
