package com.example.stockpicker.finance.controller;

import com.example.stockpicker.common.R;
import com.example.stockpicker.finance.dto.CategoryForm;
import com.example.stockpicker.finance.entity.FinCategory;
import com.example.stockpicker.finance.service.FinanceService;
import com.example.stockpicker.security.RequirePerm;
import com.example.stockpicker.security.UserContext;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/finance/categories")
public class CategoryController {

    private final FinanceService financeService;

    public CategoryController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping
    @RequirePerm("fin:category:list")
    public R<List<FinCategory>> list(@RequestParam(required = false) Integer type) {
        return R.ok(financeService.listCategories(UserContext.require().id(), type));
    }

    @PostMapping
    @RequirePerm("fin:category:add")
    public R<FinCategory> create(@RequestBody @Valid CategoryForm form) {
        return R.ok(financeService.createCategory(UserContext.require().id(), form));
    }

    @PutMapping("/{id}")
    @RequirePerm("fin:category:edit")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid CategoryForm form) {
        financeService.updateCategory(UserContext.require().id(), id, form);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePerm("fin:category:delete")
    public R<Void> delete(@PathVariable Long id) {
        financeService.deleteCategory(UserContext.require().id(), id);
        return R.ok();
    }
}
