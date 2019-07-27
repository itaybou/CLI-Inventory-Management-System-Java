package logic;

import logic.datatypes.Date;
import logic.models.*;
import presentation.Prompter;
import presentation.printers.CategoryPrinter;
import presentation.printers.ReportsPrinter;
import presistence.Repository;
import presistence.dao.CategoryDAO;
import presistence.dao.DefectiveDAO;
import presistence.dao.StockDAO;

import java.util.List;
import java.util.Map;

public class ReportsProcessor implements Processor {

    private Repository repo; //A repository instance used to communicate with database
    private Prompter prompter;
    private StoreBranch activeBranch;
    private Date currentDate;

    public ReportsProcessor(Prompter prompter, Repository repo) {
        this.prompter = prompter;
        this.repo = repo;
    }

    @Override
    public void process() {
        boolean menu = false;
        while (!menu) {
            switch (prompter.getPrinter(Modules.REPORTS).printMenu()) {
                case 1:
                    prompter.printMessage(false, null);
                    processNotifications();
                    break;
                case 2:
                    prompter.printMessage(false, null);
                    processInventoryReport();
                    break;
                case 3:
                    prompter.printMessage(false, null);
                    processDefectedReport();
                    break;
                case 4:
                    menu = true;
                    break;
            }
        }
    }

    private void processInventoryReport() {

        switch(((ReportsPrinter)prompter.getPrinter(Modules.REPORTS)).chooseCategoryAll())
        {
            case 1:
                prompter.printMessage(false, null);
                List<StockProducts> inventory = ((StockDAO)repo.getDAO(Modules.STOCK)).findAllInventory(activeBranch, null);
                ((ReportsPrinter)prompter.getPrinter(Modules.REPORTS)).printInventory(null, inventory, true);
                prompter.printMessage(false, null);
                break;
            case 2:
                prompter.printMessage(false, null);
                List<Integer> ids = ((CategoryPrinter)prompter.getPrinter(Modules.CATEGORIES)).multipleCategoryIDPrompt();
                prompter.printMessage(false, null);
                ProductCategory category = null;
                int i = 0;
                if(ids.size() != 0) {
                    for (Integer categoryID : ids) {
                        if ((category = ((CategoryDAO) repo.getDAO(Modules.CATEGORIES)).findCategoryByKey(categoryID)) != null) {
                            List<StockProducts> category_inventory = ((StockDAO) repo.getDAO(Modules.STOCK)).findAllInventory(activeBranch, category);
                            if(i == ids.size()-1)
                                ((ReportsPrinter) prompter.getPrinter(Modules.REPORTS)).printInventory(category, category_inventory, true);
                            else ((ReportsPrinter) prompter.getPrinter(Modules.REPORTS)).printInventory(category, category_inventory, false);
                        } else {
                            if(i == ids.size()-1)
                                ((ReportsPrinter) prompter.getPrinter(Modules.REPORTS)).printCategoryNotExist(categoryID, true);
                            else ((ReportsPrinter) prompter.getPrinter(Modules.REPORTS)).printCategoryNotExist(categoryID, false);
                        }
                        i++;
                    }
                } else prompter.printMessage(true, "Did not enter any Category ID numbers.");
                prompter.printMessage(false, null);
                break;
            case 3:
                prompter.printMessage(false, null);
                break;
        }
    }

    private void processDefectedReport() {
        List<DefectiveProduct> defects_total = ((DefectiveDAO)repo.getDAO(Modules.DEFECTS)).findAllTotal(activeBranch);
        List<DefectiveProduct> defects_by_location = ((DefectiveDAO)repo.getDAO(Modules.DEFECTS)).findAll(activeBranch, null);
        ((ReportsPrinter)prompter.getPrinter(Modules.REPORTS)).reportDefects(defects_total, defects_by_location);
        prompter.printMessage(false, null);
    }

    private void processNotifications() {

        Map.Entry<List<StockProducts>, List<StockProducts>> expired = ((StockDAO)repo.getDAO(Modules.STOCK)).findExpired(activeBranch);
        List<StockProducts> critical_amount = ((StockDAO)repo.getDAO(Modules.STOCK)).findCriticalAmount(activeBranch);
        if(expired.getKey().size() != 0 || critical_amount.size() != 0) {
            prompter.promptNotifications(expired.getKey(), critical_amount, currentDate);
            prompter.printMessage(false, null);
        }
        else prompter.printMessage(true, "No new notification currently available.");
    }

    public void setActiveBranch(StoreBranch activeBranch) {
        this.activeBranch = activeBranch;
    }


    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }
}
