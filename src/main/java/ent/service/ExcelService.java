package ent.service;

import ent.Bot;
import ent.button.MarkupBoards;
import ent.entity.Deliver;
import ent.entity.Template;
import ent.entity.product.Product;
import ent.repo.ProductRepo;
import ent.repo.TemplateRepo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class ExcelService {

    private final TemplateRepo templateRepo;
    private final ProductRepo productRepo;
    private Sheet sheet;

    public ExcelService(TemplateRepo templateRepo, ProductRepo productRepo) {
        this.templateRepo = templateRepo;
        this.productRepo = productRepo;
    }

    private boolean loadWorkbook(String path) {
        try (FileInputStream fs = new FileInputStream(path)) {
            Workbook w = new XSSFWorkbook(fs);
            sheet = w.getSheetAt(0);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void readData(String path, LocalDateTime day, Update update, Bot bot, MarkupBoards markup) {
        ExecutorService service = Executors.newFixedThreadPool(4);
        Runnable runnable = (() -> {
            if (templateRepo.existsByDate(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))))
                templateRepo.deleteByDate(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            if (!loadWorkbook(path)) {
                SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), "<i>Ushbu fayl bot tushunadigan ko'rinishda emas!</i>");
                sendMessage.enableHtml(true);
                sendMessage.setReplyMarkup(markup.adminPanel());
                bot.executeMessage(sendMessage);
            }
            try {
                List<Product> products = getProduct();
                Template template = createTemplate(products, day);
                Objects.requireNonNull(template).setDate(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                SendMessage sendMessage = new SendMessage(update.getMessage().getChatId().toString(), "<b>Jadval yuklandi!</b>");
                sendMessage.setReplyMarkup(markup.adminPanel());
                sendMessage.enableHtml(true);
                bot.executeMessage(sendMessage);
                templateRepo.save(template);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        service.submit(runnable);

    }

    private Template createTemplate(List<Product> products, LocalDateTime day) {
        try {
            Template template = new Template();
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();
            rowIterator.next();
            for (Product product : products) {
                Row row = sheet.getRow(product.getRowIndex());
                product.setDelivers(new ArrayList<>());
                List<Deliver> delivers1 = getDelivers();
                for (Deliver deliver : delivers1) {
                    Cell cell = row.getCell(deliver.getColIndex());
                    product.setDay(day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                    deliver.setProductCount((int) Math.round(cell.getNumericCellValue()));
                    product.setTotalCount(Math.round(row.getCell(row.getLastCellNum() - 1).getNumericCellValue()));
                    product.setCount(product.getTotalCount());
                    product.setTemplate(template);
                    deliver.setPercent(((deliver.getProductCount() / product.getTotalCount())));
                    if ((deliver.getProductCount() > 0)) deliver.setPresentInProduct(true);
                    product.getDelivers().add(deliver);
                    deliver.setProduct(product);
                }
            }
            template.setEdited(false);
            template.setProducts(products);
            template.setAllProductCount((long) products.stream().collect(Collectors.summarizingDouble(Product::getTotalCount)).getSum());
            return template;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Product> getProduct() {
        List<Product> products = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        rowIterator.next();
        while (rowIterator.hasNext()) {
            Row nextRow = rowIterator.next();
            Cell cell = nextRow.getCell(1);
            Product p = Product.builder().name(cell.getStringCellValue()).count(0).rowIndex(cell.getRowIndex()).build();
            products.add(p);
        }
        products.remove(products.size() - 1);
        return products;
    }

    private List<Deliver> getDelivers() {
        List<Deliver> delivers = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next();
        Row users = rowIterator.next();
        Iterator<Cell> userColumns = users.cellIterator();
        userColumns.next();
        userColumns.next();
        while (userColumns.hasNext()) {
            Cell nextCell = userColumns.next();
            Deliver d = Deliver.builder().username(nextCell.getStringCellValue()).colIndex(nextCell.getColumnIndex()).build();
            delivers.add(d);
        }
        delivers.remove(delivers.size() - 1);
        return delivers;
    }

    public String editFile(Template template, LocalDateTime day, Service service) throws IOException {
        String format = day.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        File downloads = new File("downloads");
        String path = "";
        File[] files = downloads.listFiles();
        if (Objects.nonNull(files)) {
            for (File file : files) {
                if (file.getName().contains(format)) {
                    path = file.getName();
                    break;
                }
            }
        }
        Workbook workbook = new XSSFWorkbook("downloads" + File.separator + path);
        Sheet sheet = workbook.getSheetAt(0);
        List<Product> products = template.getProducts();
        for (Product product : products) {
            if (product.isEdited()) {
                Row row = sheet.getRow(product.getRowIndex());
                List<Deliver> delivers = product.getDelivers();
                for (Deliver deliver : delivers) {
                    Cell cell = row.getCell(deliver.getColIndex());
                    cell.setCellValue(deliver.getProductCount());
                    Cell overall = row.getCell(row.getLastCellNum() - 1);
                    overall.setCellValue(product.getTotalCount());
                }
            }
        }
        sheet.removeRow(sheet.getRow(sheet.getLastRowNum()));
        File directory = new File("changes");
        if (!directory.exists()) directory.mkdir();
        File f = new File("changes" + File.separator + format + " sana uchun o`zgarishlar.xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(f)) {
            workbook.write(outputStream);
            workbook.close();
        }
        template.setEdited(true);
        templateRepo.save(template);
        service.saveAllProduct(products);
        return f.getPath();
    }
}
