import java.util.Observable;
import java.util.Observer;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// Strategy Pattern: WeatherDataCollectionStrategy
interface WeatherDataCollectionStrategy {
    void collectData(WeatherData weatherData);
}

class WeatherAPI implements WeatherDataCollectionStrategy {
    public void collectData(WeatherData weatherData) {
        // Simulate fetching weather data from an API with random temperature
        double temperatureCelsius = Math.random() * 50 - 10;
        double humidity = Math.random() * 50 + 50;
        double pressure = Math.random() * 10 + 1013;
        weatherData.setMeasurements(temperatureCelsius, humidity, pressure);
    }
}

class WeatherSensor implements WeatherDataCollectionStrategy {
    public void collectData(WeatherData weatherData) {
        // Simulate collecting weather data from sensors with random temperature
        double temperatureCelsius = Math.random() * 50 - 10;
        double humidity = Math.random() * 50 + 50;
        double pressure = Math.random() * 10 + 1013;
        weatherData.setMeasurements(temperatureCelsius, humidity, pressure);
    }
}

// Observer Pattern: WeatherObserver
class WeatherObserver implements Observer {
    private WeatherData weatherData;
    private double temperatureThreshold;

    public WeatherObserver(WeatherData weatherData, double temperatureThreshold) {
        this.weatherData = weatherData;
        this.temperatureThreshold = temperatureThreshold;
        weatherData.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof WeatherData) {
            WeatherData weatherData = (WeatherData) o;
            double temperature = weatherData.getTemperatureCelsius();

            if (temperature < temperatureThreshold) {
                System.out.println("Temperature is below " + temperatureThreshold + "°C. Warning!");
            }
        }
    }
}

// Decorator Pattern: WeatherReportDecorator
abstract class WeatherReportDecorator {
    protected WeatherData weatherData;

    public WeatherReportDecorator(WeatherData weatherData) {
        this.weatherData = weatherData;
    }

    public abstract void display();
}

class TemperatureDecorator extends WeatherReportDecorator {
    private String temperatureScale;

    public TemperatureDecorator(WeatherData weatherData, String temperatureScale) {
        super(weatherData);
        this.temperatureScale = temperatureScale;
    }

    @Override
    public void display() {
        String scaleLabel = "";
        double temperature = 0.0;

        if (temperatureScale.equalsIgnoreCase("Celsius")) {
            scaleLabel = "°C";
            temperature = weatherData.getTemperatureCelsius();
        } else if (temperatureScale.equalsIgnoreCase("Fahrenheit")) {
            scaleLabel = "°F";
            temperature = weatherData.getTemperatureFahrenheit();
        } else if (temperatureScale.equalsIgnoreCase("Kelvin")) {
            scaleLabel = "K";
            temperature = weatherData.getTemperatureKelvin();
        } else {
            System.out.println("Invalid temperature scale. Using the default scale (Celsius).");
            scaleLabel = "°C";
            temperature = weatherData.getTemperatureCelsius();
        }

        System.out.println("Temperature (" + temperatureScale + "): " + temperature + scaleLabel);
    }
}

// State Pattern: WeatherConditionState
interface WeatherConditionState {
    void display();
}

class SunnyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Sunny");
    }
}

class RainyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Rainy");
    }
}

class CloudyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Cloudy");
    }
}

class SnowyState implements WeatherConditionState {
    public void display() {
        System.out.println("Weather: Snowy");
    }
}

// Singleton Pattern: WeatherData
class WeatherData extends Observable {
    private static WeatherData instance;
    private static final String URL = "jdbc:postgresql://localhost:5432/tutdb"; // Replace with your PostgreSQL URL
    private static final String USER = "postgres"; // Replace with your PostgreSQL username
    private static final String PASSWORD = "h1H1h2H2"; // Replace with your PostgreSQL password

    private double temperatureCelsius;
    private double humidity;
    private double pressure;
    private WeatherConditionState conditionState;

    private WeatherData() {
        // Private constructor to prevent external instantiation.
    }

    public static WeatherData getInstance() {
        if (instance == null) {
            instance = new WeatherData();
        }
        return instance;
    }

    public void setMeasurements(double temperatureCelsius, double humidity, double pressure) {
        this.temperatureCelsius = temperatureCelsius;
        this.humidity = humidity;
        this.pressure = pressure;

        if (temperatureCelsius > 25.0) {
            conditionState = new SunnyState();
        } else if (temperatureCelsius > 15.0) {
            conditionState = new CloudyState();
        } else if (temperatureCelsius > 0.0) {
            conditionState = new RainyState();
        } else {
            conditionState = new SnowyState();
        }

        measurementsChanged();
    }

    public void measurementsChanged() {
        setChanged();
        notifyObservers();
    }

    public double getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public double getTemperatureFahrenheit() {
        return (temperatureCelsius * 9 / 5) + 32;
    }

    public double getTemperatureKelvin() {
        return temperatureCelsius + 273.15;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public void displayWeatherCondition() {
        conditionState.display();
    }

    public void setWeatherDataInDatabase(String time) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            String query = "INSERT INTO weather_data (time, temperature, humidity, pressure) VALUES (?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, time);
            preparedStatement.setDouble(2, temperatureCelsius);
            preparedStatement.setDouble(3, humidity);
            preparedStatement.setDouble(4, pressure);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (preparedStatement != null) preparedStatement.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

public class WeatherApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WeatherData weatherData = WeatherData.getInstance();
        String temperatureScale = "Celsius";
        String selectedTime = "2023-11-07 12:00:00";

        System.out.println("Weather Monitoring and Alert System");
        System.out.println("Enter 'q' to exit.");

        while (true) {
            System.out.println("<=======================Welcome=======================>");
            System.out.println("Menu:");
            System.out.println("1. Collect Weather Data");
            System.out.println("2. Choose Temperature Scale (Celsius/Fahrenheit/Kelvin)");
            System.out.println("3. Set Temperature Alert Threshold");
            System.out.println("4. Change Data Source (API/Sensor)");
            System.out.println("5. Display Weather Data");
            System.out.println("6. Display Weather Condition");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            if ("q".equalsIgnoreCase(choice)) {
                break;
            } else if ("1".equals(choice)) {
                System.out.print("Choose Data Source (API/Sensor): ");
                String input = scanner.nextLine();
                WeatherDataCollectionStrategy dataCollectionStrategy;

                if ("API".equalsIgnoreCase(input)) {
                    dataCollectionStrategy = new WeatherAPI();
                } else if ("Sensor".equalsIgnoreCase(input)) {
                    dataCollectionStrategy = new WeatherSensor();
                } else {
                    System.out.println("Invalid choice. Please enter 'API' or 'Sensor'.");
                    continue;
                }

                dataCollectionStrategy.collectData(weatherData);

                // After collecting data, add it to the database
                weatherData.setWeatherDataInDatabase(selectedTime);
            } else if ("3".equals(choice)) {
                System.out.print("Enter a new temperature threshold (" + temperatureScale + "): ");
                double threshold = convertTemperature(Double.parseDouble(scanner.nextLine()), temperatureScale, "Celsius");
                WeatherObserver temperatureObserver = new WeatherObserver(weatherData, threshold);
            } else if ("4".equals(choice)) {
                System.out.print("Choose Data Source (API/Sensor): ");
                String input = scanner.nextLine();
                WeatherDataCollectionStrategy dataCollectionStrategy;

                if ("API".equalsIgnoreCase(input)) {
                    dataCollectionStrategy = new WeatherAPI();
                } else if ("Sensor".equalsIgnoreCase(input)) {
                    dataCollectionStrategy = new WeatherSensor();
                } else {
                    System.out.println("Invalid choice. Please enter 'API' or 'Sensor'.");
                    continue;
                }

                dataCollectionStrategy.collectData(weatherData);

                // After collecting data, add it to the database
                weatherData.setWeatherDataInDatabase(selectedTime);
            } else if ("5".equals(choice)) {
                System.out.println("<==============================================================>");
                System.out.println("Last Weather Data:");
                WeatherReportDecorator temperatureDecorator = new TemperatureDecorator(weatherData, temperatureScale);
                temperatureDecorator.display();
                System.out.println("Humidity: " + weatherData.getHumidity() + "%");
                System.out.println("Pressure: " + weatherData.getPressure() + " hPa");
                System.out.println("<==============================================================>");
            } else if ("6".equals(choice)) {
                weatherData.displayWeatherCondition();
            } else if ("2".equals(choice)) {
                System.out.print("Choose Temperature Scale (Celsius/Fahrenheit/Kelvin): ");
                temperatureScale = scanner.nextLine();
            } else {
                System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    private static double convertTemperature(double value, String fromScale, String toScale) {
        if (fromScale.equals(toScale)) {
            return value;
        }
        if (fromScale.equals("Celsius")) {
            if (toScale.equals("Fahrenheit")) {
                return (value * 9 / 5) + 32;
            } else if (toScale.equals("Kelvin")) {
                return value + 273.15;
            }
        } else if (fromScale.equals("Fahrenheit")) {
            if (toScale.equals("Celsius")) {
                return (value - 32) * 5 / 9;
            } else if (toScale.equals("Kelvin")) {
                return (value + 459.67) * 5 / 9;
            }
        } else if (fromScale.equals("Kelvin")) {
            if (toScale.equals("Celsius")) {
                return value - 273.15;
            } else if (toScale.equals("Fahrenheit")) {
                return (value * 9 / 5) - 459.67;
            }
        }
        return value;
    }
}
