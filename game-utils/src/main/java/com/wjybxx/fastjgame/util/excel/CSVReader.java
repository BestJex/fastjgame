/*
 * Copyright 2019 wjybxx
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wjybxx.fastjgame.util.excel;

import com.wjybxx.fastjgame.util.CloseableUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;

/**
 * CSV文件的Reader。
 * CSV文件不支持分页，也就是sheetIndex必须为0；
 *
 * @author wjybxx
 * @version 1.0
 * date - 2019/5/11 16:17
 * github - https://github.com/hl845740757
 */
class CSVReader extends TableReader<CSVRecord> {

    /**
     * windows上的CSV编码为GBK
     */
    private static final Charset windowsCharset = Charset.forName("GBK");
    /**
     * 文件编码集
     */
    private final Charset charset;
    /**
     * CSV文件解析器
     */
    private CSVParser parser = null;

    public CSVReader() {
        this(windowsCharset);
    }

    /**
     * create instance
     *
     * @param charset CSV 支持指定编码，默认GBK
     */
    public CSVReader(Charset charset) {
        this.charset = charset;
    }

    @Override
    protected int getTotalColNum(CSVRecord row) {
        return row.size();
    }

    @Override
    protected Iterator<CSVRecord> toIterator(File file, int sheetIndex) throws IOException {
        if (sheetIndex != 0) {
            throw new IllegalArgumentException("csv reader only support sheetIndex 0");
        }
        parser = CSVParser.parse(file, charset, CSVFormat.DEFAULT);
        return parser.iterator();
    }

    @Override
    protected String getNullableCell(CSVRecord row, int colIndex) {
        return row.get(colIndex);
    }

    @Override
    public void close() throws Exception {
        CloseableUtils.closeSafely(parser);
    }
}
