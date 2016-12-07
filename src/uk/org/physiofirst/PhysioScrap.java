package uk.org.physiofirst;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;

public class PhysioScrap {

	public static void main(String args[]) {
		File pathToBinary = new File("D:\\APPS\\FF\\firefox.exe");
		FirefoxBinary ffBinary = new FirefoxBinary(pathToBinary);
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		firefoxProfile.setPreference("permissions.default.image", 2);
		
		//FF
		FirefoxDriver driver = new FirefoxDriver(ffBinary, firefoxProfile);
		
		//CHROME
		//System.setProperty("webdriver.chrome.driver", "D:\\Download\\chromedriver_win32\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();
		
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println("Start at: " + dateFormat.format(date));

		Workbook wb = null;
		try {
			wb = WorkbookFactory.create(new File("out.xlsx"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Sheet sh = wb.getSheetAt(0);

		String strona = "http://www.physiofirst.org.uk/find-physio/search-physio.html?q=";
		driver.get(strona);

		By byLoadMore = By.xpath(".//a[@id='load-more-practice']");
		By byListItem = By.className("articles-item");
		By byNameAddressText = By.xpath(".//div[@class='articles-item-details']//div[1]/p");
		By byEmailUs = By.xpath(".//a[text() = 'Email us']");
		By byVisitOurSite = By.xpath(".//a[text() = 'Visit our site']");
		By byPhone = By.xpath(".//div[@class='row articles-item-details-contact']/div/ul/li[(contains(., 'Tel')) or (contains(., 'Mob'))]");
		By byPracticeName = By.className("blue-grey-light");

		int rownum = 0;

		long start_time = 0;
		long end_time = 0;
		long difference = 0;
		//int startpos = 0;
		//int endpos = 9;
		String profession = "Physiotherapist";
		
		while (true) {
			 
			//List<WebElement> paczka = driver.findElements(byListItem);
			
			start_time = System.currentTimeMillis();
			driver.findElement(byLoadMore).click();
			end_time = System.currentTimeMillis();
			difference = end_time - start_time;
			rownum++;
			System.out.println("now value is: " + rownum + ". Time in ms: " + difference);
			
			if ((!driver.findElement(byLoadMore).isDisplayed())) {
				System.out.println("NameAddress count: " + driver.findElements(byNameAddressText).size());
				System.out.println("byEmailUs count: " + driver.findElements(byEmailUs).size());
				System.out.println("byVisitOurSite count: " + driver.findElements(byVisitOurSite).size());
				System.out.println("byPhone count: " + driver.findElements(byPhone).size());
				break;
			}
		}

		rownum = 0;
		//List<WebElement> rekordy = driver.findElements(byListItem);
		int index = 0;
		int size = driver.findElements(byListItem).size();
		int refno = 40000;
		
		for (int it = size; it > 0; it--) {
			//WebElement we = rekordy.get(it);
			
			String byListItemText = ".//*[@class='articles-item']" + "[" + Integer.toString(it)  + "]";
			WebElement we = driver.findElement(By.xpath(byListItemText));
			
			
			index++;
			
			start_time = System.currentTimeMillis();
			rownum++;
			sh.createRow(rownum);
			Row r1 = sh.getRow(rownum);

			List<WebElement> practiceName = we.findElements(byPracticeName);
			List<WebElement> nameAddress = we.findElements(byNameAddressText);
			List<WebElement> emailSection = we.findElements(byEmailUs);
			List<WebElement> phoneSection = we.findElements(byPhone);
			List<WebElement> visitUs = we.findElements(byVisitOurSite);

			String practiceNameString = (practiceName.size() == 0) ? "NULL" : practiceName.get(0).getText();
			
			String fullNameText = (nameAddress.size() == 0) ? "NULL" : nameAddress.get(0).getText();
			String[] nametab = fullNameText.split("\\r?\\n");
			String name = nametab[0];
			String address = "";
			for (int a = 1; a < nametab.length; a++) {
				address = address.concat(nametab[a]).concat(", ");
			}
			address = address.substring(0,address.lastIndexOf(","));

			String email = (emailSection.size() == 0) ? ""
					: emailSection.get(0).getAttribute("href").substring(emailSection.get(0).getAttribute("href").indexOf(":") + 1);
			String website = (visitUs.size() == 0) ? "" : visitUs.get(0).getAttribute("href");
			String phone = "";
			if (phoneSection.size() == 0) {
				phone = "";
			} else {
				for (int b = 0; b < phoneSection.size(); b++) {
					if (phoneSection.get(b).getText().contains("Tel"))
						phone = phoneSection.get(b).getText().substring(phoneSection.get(b).getText().indexOf(": ") + 1);
					else if (phoneSection.get(b).getText().contains("Mob"))
						phone = phoneSection.get(b).getText().substring(phoneSection.get(b).getText().indexOf(": ") + 1);
					// phone.concat(phoneSection.get(b).getText()).concat("\r\n");
				}
			}

			r1.createCell(0).setCellValue(refno);
			r1.createCell(1).setCellValue(name);
			r1.createCell(2).setCellValue(profession);
			r1.createCell(3).setCellValue(practiceNameString);
			r1.createCell(4).setCellValue(address);
			r1.createCell(5).setCellValue(phone);
			r1.createCell(6).setCellValue(email);
			r1.createCell(7).setCellValue(website);

			refno++;
			
			FileOutputStream out = null;
			try {
				out = new FileOutputStream("result.xlsx");
				wb.write(out);
				out.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			end_time = System.currentTimeMillis();
			difference = end_time - start_time;
			System.out.println("Processing record " + index + " of " + size + ". Time in ms: " + difference);
		}

		driver.quit();
		date = new Date();
		System.out.println("End at: " + dateFormat.format(date));

		return;

	}

}
