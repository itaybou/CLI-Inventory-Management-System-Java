package presentation.printers;

import logic.models.StoreBranch;
import presentation.Prompter;
import presentation.TableList;

import java.io.IOException;
import java.util.List;

import static org.fusesource.jansi.Ansi.ansi;

public class BranchesPrinter extends Prompter implements Printer {

    public int printMenu()
    {
        boolean valid = false;
        while (!valid) {
            System.out.println(ansi().bold().render(
                    "@|green ~ WELCOME, Choose you action: ~|@\n" +
                            "@|yellow 1|@ - Manage Store Branch\n" +
                            "@|yellow 2|@ - Open A new Store Branch\n" +
                            "@|yellow 3|@ - Close existing Store branch\n" +
                            "@|yellow 4|@ - Display all Chain Stores\n" +
                            "@|yellow 5|@ - EXIT\n"));
            return menuSelection(1, 4);
        }
        return ILLEGAL;
    }

    public int chooseBranch(boolean warn) throws IOException {
        System.out.println(ansi().bold().render("@|white Press ENTER to confirm.|@"));
        System.out.print(ansi().bold().render(warn? "@|red Enter branch ID number to delete: |@" : "@|green Enter branch ID number:  |@"));
        String branch = reader.readLine();
        return Integer.parseInt(branch);
    }

    public String promptNewBranchName() throws IOException{
        System.out.println(ansi().bold().render("@|white Press ENTER to confirm\nEnter q\\Q to cancel.|@"));
        System.out.println(ansi().bold().render("@|green Enter new store branch name: |@"));
        return reader.readLine();
    }

    public void printChainBranches(List<StoreBranch> branches) {
        printMessage(false, "Press ENTER to return");
        System.out.println(ansi().render("@|yellow DISPLAYING ALL CHAIN STORES:\n |@"));
        if(branches.size() == 0) {
            System.out.println(ansi().render("@|red No opened stores exist currently. |@"));
        } else {
            System.out.println(ansi().render("Showing @|cyan " + branches.size() + "|@ existing opened store branches.\n"));
            TableList branchTable = new TableList(2,
                    "Store Branch ID", "Store Name")
                    .sortBy(1).withUnicode(false);
            branches.forEach(branch -> branchTable.addRow(branch.getBranchID()+"", branch.getName()));

            branchTable.print();
        }
        scan.nextLine();
    }
}
