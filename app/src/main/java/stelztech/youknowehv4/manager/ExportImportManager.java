package stelztech.youknowehv4.manager;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.Toast;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import stelztech.youknowehv4.activitypackage.MainActivityManager;
import stelztech.youknowehv4.database.DatabaseManager;
import stelztech.youknowehv4.model.Card;
import stelztech.youknowehv4.model.Deck;

/**
 * Created by alex on 2017-05-02.
 */


public final class ExportImportManager {


    public final static String TAG = "ExportImportManager";

    public final static String storingFolder = "/YouKnowEh/Export";


    public static File saveExcelFile(Context context, Deck deckToExport, List<Card> cardList) {

        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(context, "Storage not available or read only", Toast.LENGTH_SHORT).show();
            return null;
        }
        ;

        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        String deckName = deckToExport.getDeckName();
        int numberOfCards = cardList.size();

        if (numberOfCards < 1) {
            Toast.makeText(context, "Cannot export deck with no cards", Toast.LENGTH_SHORT).show();
            return null;
        }

        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet(deckName);

        for (int counter = 0; counter < numberOfCards; counter++) {
            Row row = sheet1.createRow(counter);

            Card cardTemp = cardList.get(counter);
            String question = cardTemp.getQuestion();
            String answer = cardTemp.getAnswer();
            String moreinfo = cardTemp.getMoreInfo();
            String id = cardTemp.getCardId();

            c = row.createCell(0);
            c.setCellValue(question);
            c = row.createCell(1);
            c.setCellValue(answer);
            c = row.createCell(2);
            c.setCellValue(moreinfo);
//            c = row.createCell(3);
//            c.setCellValue(id);

        }


        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));
        sheet1.setColumnWidth(3, (15 * 500));


//        File file = new File(context.getExternalFilesDir(null), deckToExport.getDeckName());

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + storingFolder);
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) {
                Toast.makeText(context, "Give app permission to access storage to export", Toast.LENGTH_SHORT).show();
                return null;
            }
        }


        File file = new File(dir, deckName + ".xlsx");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
        } catch (IOException e) {
            Toast.makeText(context, "Give app permission to access storage to export", Toast.LENGTH_SHORT).show();
            return null;
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            try {
                if (null != os)
                    os.close();

            } catch (Exception ex) {
            }
        }


        return file;
    }

    public static boolean readExcelFile(Context context, Uri uri) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(context, "Storage not available or read only", Toast.LENGTH_SHORT).show();
            return false;
        }


        List<CardHolder> cardHolderList = new ArrayList<CardHolder>();
        String fileName = getFileName(context, uri);
        try {
            // Creating Input Stream


            if (fileName.contains(".")) {
                int index = fileName.indexOf(".");
                fileName = fileName.substring(0, index);
            }

            Toast.makeText(context, fileName, Toast.LENGTH_SHORT);

            File file = new File(uri.getPath());


            InputStream myInput = context.getContentResolver().openInputStream(uri);


            // Create a POIFSFileSystem object
//            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
//            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
//            Workbook workbook = WorkbookFactory.create(myInput);


            XSSFWorkbook wb = new XSSFWorkbook(myInput);

            // Get the first sheet from workbook
            XSSFSheet mySheet = wb.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator rowIter = mySheet.rowIterator();


            while (rowIter.hasNext()) {
                Row myRow = mySheet.getRow(0);


                Iterator cellIter = myRow.cellIterator();
                int rowCounter = 0;

                String question = "";
                String answer = "";
                String note = "";

                while (cellIter.hasNext()) {
                    Cell myCell = (Cell) cellIter.next();

                    if (rowCounter == 0) {
                        question = myCell.toString();
                    } else if (rowCounter == 1) {
                        answer = myCell.toString();
                    } else if (rowCounter == 2) {
                        note = myCell.toString();
                    } else {
                        Toast.makeText(context, "Invalid file format", Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    rowCounter++;
                }

                if (question.isEmpty() || answer.isEmpty()) {
                    Toast.makeText(context, "Invalid file format - two first column cannot be empty", Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    cardHolderList.add(new CardHolder(question, answer, note));
                }


            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }

        if(cardHolderList.isEmpty()){
            Toast.makeText(context, "File is empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        DatabaseManager dbManager = DatabaseManager.getInstance(context);
        String deckId = dbManager.createDeck(fileName);

        for (int i = 0; i < cardHolderList.size(); i++){
            String question = cardHolderList.get(i).getQuestion();
            String answer = cardHolderList.get(i).getAnswer();
            String note = cardHolderList.get(i).getNote();
            String cardId = dbManager.createCard(question, answer, note);
            dbManager.createCardDeck(cardId, deckId);
        }

        Toast.makeText(context, "Deck \"" + fileName + "\" imported", Toast.LENGTH_SHORT).show();
        return true;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static void exportFileToEmail(Context context, Deck deckToExport, List<Card> cardList) {

        File file = saveExcelFile(context, deckToExport, cardList);

        if (file != null) {
            Uri U = Uri.fromFile(file);
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");
            emailIntent.putExtra(Intent.EXTRA_STREAM, U);


            context.startActivity(emailIntent);
        }
    }

    public static void importDeck(Context context, Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");   //xlxs only
//        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            activity.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),
                    MainActivityManager.EXPORT_RESULT);
            Toast.makeText(context, "Select a Deck to import", Toast.LENGTH_SHORT).show();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }

    }

    private static String getFileName(Context context, Uri uri) {
        String fileName = "";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        return fileName;

    }


    private static class CardHolder {
        private String question;
        private String answer;
        private String note;

        public CardHolder(String question, String answer, String note) {
            this.question = question;
            this.answer = answer;
            this.note = note;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public String getAnswer() {
            return answer;
        }

        public void setAnswer(String answer) {
            this.answer = answer;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }
}
