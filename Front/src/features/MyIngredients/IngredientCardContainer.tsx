// src/features/MyIngredients/IngredientCardContainer.tsx

import React, { useState, useEffect, useMemo } from "react";
import { useIngredients } from "../../contexts/IngredientsContext";
import TopTabMenu from "./TopTabMenu";
import IngredientCard from "./IngredientCard";
import PaginationControls from "./PaginationControls";
import { Ingredient } from "../../types/EntityTypes";
import { IngredientStatus } from "../../types/FeatureTypes";
import { calculateStatus } from "../../utils/Utils";

interface IngredientCardContainerProps {
  onAddClick: () => void;
  onCardClick: (ingredient: Ingredient) => void;
}

const IngredientCardContainer: React.FC<IngredientCardContainerProps> = ({
  onAddClick,
  onCardClick,
}) => {
  const { ingredients } = useIngredients();
  const [activeSort, setActiveSort] = useState<string>("status");
  const [sortDirection, setSortDirection] = useState<boolean>(true);
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [itemsPerPage, setItemsPerPage] = useState<number>(12);
  const [columns, setColumns] = useState<number>(3);

  useEffect(() => {
    const calculateItemsPerPage = () => {
      const width = window.innerWidth;
      const height = window.innerHeight;

      const headerHeight = 60;
      const footerHeight = 120;
      const availableHeight = height - headerHeight - footerHeight;

      const cardWidth = 240;
      const cardHeight = 120;
      const horizontalGap = 8;
      const verticalGap = 8;

      const cols = Math.floor((width + horizontalGap) / (cardWidth + horizontalGap));
      const rows = Math.floor((availableHeight + verticalGap) / (cardHeight + verticalGap));

      setColumns(cols);
      setItemsPerPage(cols * rows);
    };

    calculateItemsPerPage();
    window.addEventListener("resize", calculateItemsPerPage);

    return () => {
      window.removeEventListener("resize", calculateItemsPerPage);
    };
  }, []);

  const getStatusPriority = (ingredient: Ingredient): number => {
    const status = calculateStatus(ingredient);
    switch (status) {
      case IngredientStatus.Expired:
        return 1;
      case IngredientStatus.Caution:
        return 2;
      case IngredientStatus.Safe:
        return 3;
      default:
        return 4;
    }
  };

  const sortedIngredients = useMemo(() => {
    let sorted = [...ingredients];
    switch (activeSort) {
      case "status":
        sorted.sort((a, b) => {
          const aPriority = getStatusPriority(a);
          const bPriority = getStatusPriority(b);
          return sortDirection ? aPriority - bPriority : bPriority - aPriority;
        });
        break;
      case "name":
        sorted.sort((a, b) => {
          const comparison = a.name.localeCompare(b.name);
          return sortDirection ? comparison : -comparison;
        });
        break;
      case "quantity":
        sorted.sort((a, b) => (sortDirection ? b.quantity - a.quantity : a.quantity - b.quantity));
        break;
    }
    return sorted;
  }, [activeSort, sortDirection, ingredients]);

  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = sortedIngredients.slice(indexOfFirstItem, indexOfLastItem);

  const totalPages = Math.ceil(sortedIngredients.length / itemsPerPage);

  return (
    <div style={{ position: "relative", paddingBottom: "7rem" }}>
      <TopTabMenu
        activeSort={activeSort}
        sortDirection={sortDirection}
        onSortChange={(sortType) => {
          if (activeSort === sortType) {
            setSortDirection(!sortDirection);
          } else {
            setActiveSort(sortType);
            setSortDirection(true);
          }
        }}
        onAddClick={onAddClick}
      />

      <div
        className="ingredient-card-grid"
        style={{
          display: "grid",
          gridTemplateColumns: `repeat(${columns}, 1fr)`,
          gap: "16px",
          padding: "1rem",
          overflowY: "auto",
        }}
      >
        {currentItems.map((ingredient) => (
          <IngredientCard
            key={ingredient.ingredientId}
            ingredient={ingredient}
            onClick={() => onCardClick(ingredient)}
          />
        ))}
      </div>

      {/* PaginationControls 컴포넌트 */}
      <PaginationControls
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </div>
  );
};

export default IngredientCardContainer;
