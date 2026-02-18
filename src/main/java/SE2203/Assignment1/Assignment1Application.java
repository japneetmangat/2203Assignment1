package SE2203.Assignment1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;

@SpringBootApplication
@StyleSheet("styles.css")
public class Assignment1Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Assignment1Application.class, args);
    }
}
