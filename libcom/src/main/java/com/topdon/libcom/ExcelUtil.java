package com.topdon.libcom;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.UriUtils;
import com.blankj.utilcode.util.Utils;
import com.topdon.lib.core.common.SharedManager;
import com.topdon.lib.core.config.FileConfig;
import com.topdon.lib.core.db.entity.ThermalEntity;
import com.topdon.lib.core.tools.TimeTool;
import com.topdon.lib.core.tools.UnitTools;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.blankj.utilcode.util.ImageUtils.save;

public class ExcelUtil {

    @NonNull
    private static String getTemperature(int index, @NonNull byte[] norTempData, boolean isShowC) {
        int tempValue = (norTempData[2 * index + 1] << 8 & 0xff00) | (norTempData[2 * index] & 0xff);
        float value = tempValue / 64f - 273.15f;
        return UnitTools.showC(value,isShowC);
    }
    @Nullable
    public static String exportExcel(@NonNull String name, int width, int height, @NonNull byte[] norTempData, @Nullable Callback callback) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();
        boolean isShowC = SharedManager.INSTANCE.getTemperature() == 1;
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        long time = System.currentTimeMillis();
        for (int i = 0; i < height; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < width; j++) {
                int index = i * width + j;
                sheet.setColumnWidth(j, 9 * width);
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(getTemperature(index, norTempData,isShowC));
                if (index % 100 == 0 && callback != null) {
                    callback.onOneCell(index / 100, width * height / 100);
                }
            }
        }
        try {
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                File excel = new File(FileConfig.getExcelDir(), name + ".xlsx");
                FileOutputStream fos = new FileOutputStream(excel);
                workbook.write(fos);
                fos.flush();
                fos.close();
                return excel.getAbsolutePath();
            }else {
                String fileName = name + ".xlsx";
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, FileConfig.getExcelDir());
                Uri contentUri = MediaStore.Files.getContentUri("external");
                Uri uri = Utils.getApp().getContentResolver().insert(contentUri, values);
                if (uri != null) {
                    OutputStream outputStream = Utils.getApp().getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                        workbook.write(bos);
                        bos.flush();
                        bos.close();
                    }
                    Log.w("导出",UriUtils.uri2File(uri).getAbsolutePath());
                    return UriUtils.uri2File(uri).getAbsolutePath();
                }else {
                    return null;
                }
            }
        }catch (Exception e){
            return null;
        }
    }
    @FunctionalInterface
    public interface Callback {
        void onOneCell(int current, int total);
    }

    public static String exportExcel(ArrayList<ThermalEntity> listData,boolean isPoint) {
        boolean isShowC = SharedManager.INSTANCE.getTemperature() == 1;
        try {
            Workbook wb = new XSSFWorkbook();
            Sheet sheet = wb.createSheet();
            String[] title = {Utils.getApp().getString(R.string.detail_date), Utils.getApp().getString(R.string.chart_temperature_low), Utils.getApp().getString(R.string.chart_temperature_high)};
            if (isPoint){
                title = new String[]{Utils.getApp().getString(R.string.detail_date), Utils.getApp().getString(R.string.chart_temperature)};
            }
            Row row = sheet.createRow(0);
            int colNum = title.length;
            CellStyle titleStyle = wb.createCellStyle();
            titleStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            Font font =  wb.createFont();
            font.setBold(true);
            titleStyle.setFont(font);
            CellStyle contentStyle = wb.createCellStyle();
            contentStyle.setAlignment(HorizontalAlignment.CENTER);
            contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            for (int i = 0; i < colNum; i++) {
                sheet.setColumnWidth(i, 20 * 256);
                Cell cell1 = row.createCell(i);
                cell1.setCellStyle(titleStyle);
                cell1.setCellValue(title[i]);
            }
            for (int rowNum = 0; rowNum < listData.size(); rowNum++) {

                row = sheet.createRow(rowNum + 1);
                row.setHeightInPoints(28f);

                ThermalEntity bean = listData.get(rowNum);

                for (int j = 0; j < title.length; j++) {
                    Cell cell = row.createCell(j);
                    if (isPoint){
                        switch (j) {
                            case 0:
                                cell.setCellValue(bean.getTime());
                                break;
                            case 1:
                                cell.setCellStyle(contentStyle);
                                cell.setCellValue(UnitTools.showC(bean.getMinTemp()));
                                break;
                        }
                    }else {
                        switch (j) {
                            case 0:
                                cell.setCellValue(bean.getTime());
                                break;
                            case 1:
                                cell.setCellStyle(contentStyle);
                                cell.setCellValue(UnitTools.showC(bean.getMinTemp()));
                                break;
                            case 2:
                                cell.setCellStyle(contentStyle);
                                cell.setCellValue(UnitTools.showC(bean.getMaxTemp(),isShowC));
                                break;
                        }
                    }
                }
            }
            String timeStr = listData.isEmpty() ? TimeTool.INSTANCE.showDateSecond() : TimeUtils.millis2String(listData.get(0).getStartTime(), "yyyyMMddHHmmss");
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                File excel = new File(FileConfig.getExcelDir(), "TCView_"+ timeStr + ".xlsx");
                FileOutputStream fos = new FileOutputStream(excel);
                wb.write(fos);
                fos.flush();
                fos.close();
                return excel.getAbsolutePath();
            }else {
                String fileName = "TCView_"+timeStr + ".xlsx";
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, FileConfig.getExcelDir());
                Uri contentUri = MediaStore.Files.getContentUri("external");
                Uri uri = Utils.getApp().getContentResolver().insert(contentUri, values);
                if (uri != null) {
                    OutputStream outputStream = Utils.getApp().getContentResolver().openOutputStream(uri);
                    if (outputStream != null) {
                        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
                        wb.write(bos);
                        bos.flush();
                        bos.close();
                    }
                    Log.w("导出",UriUtils.uri2File(uri).getAbsolutePath());
                    return UriUtils.uri2File(uri).getAbsolutePath();
                }else {
                    return null;
                }
            }
        } catch (IOException e) {
            Log.e("ExpressExcle", "exportExcel", e);
            return null;
        }

    }
}
