package winetavern.controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import org.salespointframework.inventory.Inventory;
import org.salespointframework.inventory.InventoryItem;
import org.salespointframework.time.BusinessTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import winetavern.Helper;
import winetavern.model.DateParameter;
import winetavern.model.menu.DayMenu;
import winetavern.model.menu.DayMenuItem;
import winetavern.model.menu.DayMenuItemRepository;
import winetavern.model.menu.DayMenuRepository;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.pdf.PdfWriter;

import java.time.LocalDate;
import java.util.*;
import java.util.List;


/**
 * Created by Michel on 11/4/2016.
 * @author Michel Kunkler
 */

@Controller
public class DayMenuManager {

    private DayMenuRepository dayMenuRepository;
    private DayMenuItemRepository dayMenuItemRepository;
    private final Inventory<InventoryItem> stock;
    private BusinessTime businessTime;

    @Autowired
    public DayMenuManager(Inventory<InventoryItem> stock, DayMenuRepository dayMenuRepository,
                          DayMenuItemRepository dayMenuItemRepository,
                          BusinessTime businessTime) {
        this.businessTime = businessTime;
        this.stock = stock;
        this.dayMenuRepository = dayMenuRepository;
        this.dayMenuItemRepository = dayMenuItemRepository;
    }

    @RequestMapping("/admin/menu/show")
    public ModelAndView showMenus(ModelAndView modelAndView) {
        return showMenuList(modelAndView);
    }

    @RequestMapping("/admin/menu/add")
    public String addMenu(Model model) {
        DateParameter dateParameter = new DateParameter();
        model.addAttribute("date", dateParameter);
        return "addmenu";
    }

    @RequestMapping(value = "/admin/menu/add", method = RequestMethod.POST)
    public ModelAndView addMenuPost(@ModelAttribute(value = "date") DateParameter dateParameter,
                                    ModelAndView modelAndView) {
        dateParameter.setMonth(dateParameter.getMonth());
        LocalDate creationDate = dateParameter.getDate();
        DayMenu dayMenu = copyPreDayMenu(creationDate);
        if(dayMenu == null) {
            dayMenu = new DayMenu(creationDate);
        }
        dayMenuRepository.save(dayMenu);

        return showMenuList(modelAndView);
    }

    @RequestMapping(value = "/admin/menu/remove", method = RequestMethod.POST)
    public ModelAndView removeMenu(@RequestParam("daymenuid") Long dayMenuId, ModelAndView modelAndView) {
        // TODO This would give a NullPointerException and would not return null;
        DayMenu dayMenu = dayMenuRepository.findOne(dayMenuId).get();
        if(dayMenu != null) {
            dayMenuRepository.delete(dayMenu);
        }
        return showMenuList(modelAndView);
    }

    @RequestMapping("/admin/menu/edit/{pid}")
    public String editMenu(@PathVariable("pid") Long id, Model model) {
        DayMenu dayMenu = dayMenuRepository.findOne(id).get();
        model.addAttribute("daymenu", dayMenu);
        model.addAttribute("stock", stock);
        model.addAttribute("frommenuitemid", id);
        return "editdaymenu";
    }

    // TODO Should this really be public?
    public ModelAndView showMenuList(ModelAndView modelAndView) {
        Iterable<DayMenu> dayMenuList = dayMenuRepository.findAll();
        modelAndView.addObject("menus", dayMenuList);
        modelAndView.setViewName("daymenulist");
        return modelAndView;
    }

    public DayMenu copyPreDayMenu(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);
        DayMenu preDayMenu = dayMenuRepository.findByDay(yesterday);
        if (preDayMenu == null)
            return null;
        DayMenu newDayMenu = new DayMenu(today); //preDayMenu.clone(dayMenuItemRepository);
        dayMenuRepository.save(newDayMenu);
        for(DayMenuItem dayMenuItem : preDayMenu.getDayMenuItems()) {
            dayMenuItem.addDayMenu(newDayMenu);
        }
        return newDayMenu;
    }

    @RequestMapping("/admin/menu/print/{pid}")
    public String printMenu(@PathVariable("pid") Long id) {
        DayMenu dayMenu = dayMenuRepository.findOne(id).get();

        Document document = new Document();
        Font boldFont = new Font();
        boldFont.setStyle(Font.BOLD);
        Font catFont = new Font();
        catFont.setStyle(Font.BOLD);
        catFont.setSize(18);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("src\\main\\resources\\daymenu\\daymenu.pdf"));
            document.open();

            Paragraph title = new Paragraph("Zur fröhlichen Reblaus\n\n", catFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Map<String, List<DayMenuItem>> sortedItems = new HashMap<>();

            for (DayMenuItem item : dayMenu.getDayMenuItems()) {
                String category = Helper.getFirstItem(item.getProduct().getCategories());
                if (!sortedItems.containsKey(category))
                    sortedItems.put(category, new LinkedList<>());

                sortedItems.get(category).add(item);
            }

            for (String category : sortedItems.keySet()) {
                List<DayMenuItem> itemList = sortedItems.get(category);
                itemList.sort(Comparator.comparing(DayMenuItem::getName));

                PdfPTable menuItems = new PdfPTable(2);
                menuItems.setWidthPercentage(100);

                for (DayMenuItem item : itemList) {
                    PdfPCell cellName = new PdfPCell(new Paragraph(item.getName()));
                    cellName.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cellName.setBorderWidth(0);
                    PdfPCell cellPrice = new PdfPCell(new Paragraph(item.getPrice().getNumber().doubleValue() + "€"));
                    cellPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    cellPrice.setBorderWidth(0);
                    menuItems.addCell(cellName);
                    menuItems.addCell(cellPrice);
                }

                Paragraph categoryTitle = new Paragraph("\n" + category, boldFont);
                categoryTitle.setSpacingAfter(5);
                document.add(categoryTitle);
                document.add(menuItems);
            }

            document.close();
            writer.close();
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return "daymenupdf";
    }

}