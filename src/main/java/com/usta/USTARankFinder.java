/**
 * 
 */
package com.usta;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;

import com.usta.service.PlayerRankService;

/**
 * @author anil.bonigala
 *
 */
public class USTARankFinder {

    private static String RANKLINK_14 = "*Boys 14's Combined Standings List (Combined)";
    private static String RANKLINK_16 = "*Boys 16's Combined Standings List (Combined)";

    public static void main(String[] args) {

        getLatestRanksFromWeb(RANKLINK_14, 14);
        getLatestRanksFromWeb(RANKLINK_16, 16);
    }

    public static Map<String, Integer> getLatestRanksFromWeb(String rankText, int version) {

        System.setProperty("webdriver.chrome.driver", "E:\\chromed\\chromedriver.exe");

        // System.setProperty("webdriver.chrome.driver",
        // "/usr/bin/chromedriver");
        ChromeOptions chromeOptions = new ChromeOptions();
        // chromeOptions.addArguments("--headless");
        // chromeOptions.addArguments("--no-sandbox");

        WebDriver driver = new ChromeDriver(chromeOptions);

        String url = "https://tennislink.usta.com/tournaments/Rankings/RankingHome.aspx";

        // Navigate to URL
        driver.get(url);

        // Find text box to enter the name
        WebElement searchByPlayerName = driver.findElement(By.name("ctl00$mainContent$txtRankingPlayerName"));

        searchByPlayerName.sendKeys("Aryan Bonigala");

        WebElement submitByPlayerName = driver.findElement(By.name("ctl00$mainContent$btnSearch_PlayerRanking"));
        submitByPlayerName.click();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WebElement playerLink = driver.findElement(By.linkText("Bonigala, Aryan"));

        JavascriptExecutor jse2 = (JavascriptExecutor) driver;
        jse2.executeScript("arguments[0].click();", playerLink);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        WebElement rankLink = driver.findElement(By.linkText(rankText));
        JavascriptExecutor jse3 = (JavascriptExecutor) driver;
        jse3.executeScript("arguments[0].click();", rankLink);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, Integer> playerRanks = new HashMap<String, Integer>();

        WebElement ranges = driver.findElement(By.name("ctl00$mainContent$cboOrderOption"));
        Select rangeDropdown = new Select(ranges);

        List<WebElement> rangeOptions = rangeDropdown.getOptions();

        List<String> rangeVals = new ArrayList<String>();
        for (int i = 0; i < rangeOptions.size(); i++) {
            WebElement rangeElem = rangeOptions.get(i);
            if (!rangeElem.getAttribute("value").equalsIgnoreCase("-1")) {
                String rangeValues = rangeElem.getText();
                rangeVals.add(rangeValues);
                System.out.println("Rank range  " + rangeValues);
            }
        }

        for (Iterator iterator = rangeVals.iterator(); iterator.hasNext();) {
            String val = (String) iterator.next();

            WebElement rankRanges = driver.findElement(By.name("ctl00$mainContent$cboOrderOption"));
            Select ran = new Select(rankRanges);
            ran.selectByVisibleText(val);
            System.out.println("Getting ranks for range  " + val);

            addPlayerRanks(playerRanks, driver);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        driver.quit();

        // Sort this map by ranks which are values
        Map<String, Integer> sortedPlayerRanks = playerRanks.entrySet().stream().sorted(comparingByValue())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        for (String key : sortedPlayerRanks.keySet()) {
            // System.out.println("------------------------------------------------");
            System.out.println("Player Name: " + key + " Rank: " + sortedPlayerRanks.get(key));
        }

        PlayerRankService pRankService = new PlayerRankService();
        pRankService.savePlayerRanks(sortedPlayerRanks, version);

        return playerRanks;
    }

    private static void addPlayerRanks(Map<String, Integer> playerRanks, WebDriver driver) {

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WebElement rankTable = driver.findElement(By.xpath("//*[@id='ctl00_mainContent_grdMain2']/tbody"));

        List<WebElement> ranks = rankTable.findElements(By.tagName("tr"));

        for (int rnum = 0; rnum < ranks.size(); rnum++) {
            List<WebElement> columns = ranks.get(rnum).findElements(By.tagName("td"));
            WebElement rank = columns.get(0);
            if (!rank.getText().equalsIgnoreCase("Rank")) {
                WebElement playerName = columns.get(1);
                // System.out.println("------------------------------------------------");
                // System.out.println("player: " + playerName.getText() + "
                // rank: " + rank.getText());

                playerRanks.put(playerName.getText(), Integer.valueOf(rank.getText()));
            }
        }
    }

}
