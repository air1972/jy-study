package com.jy.study.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @GetMapping("/data")
    public List<DemoData> getDemoData() {
        return Arrays.asList(
            new DemoData(1, "示例1", "这是第一个示例数据"),
            new DemoData(2, "示例2", "这是第二个示例数据"),
            new DemoData(3, "示例3", "这是第三个示例数据")
        );
    }

    private static class DemoData {
        private final int id;
        private final String name;
        private final String description;

        public DemoData(int id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
