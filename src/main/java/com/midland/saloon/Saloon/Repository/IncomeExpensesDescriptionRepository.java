package com.midland.saloon.Saloon.Repository;

import com.midland.saloon.Saloon.Model.IncomeExpensesDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeExpensesDescriptionRepository extends JpaRepository<IncomeExpensesDescription, String> {
}
