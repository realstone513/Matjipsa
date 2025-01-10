// src/pages/MyIngredientsPage.tsx

import React from "react";
import CommonHeader from "../components/organisms/CommonHeader";
import IngredientCardContainer from "../features/MyIngredients/IngredientCardContainer";
import EditIngredientModal from "../features/MyIngredients/EditIngredientModal";
import ReceiptUploadModal from "../features/MyIngredients/ReceiptUploadModal";
import AddIngredientModal from "../features/MyIngredients/AddIngredientModal";
import IngredientOptionsModal from "../features/MyIngredients/IngredientOptionModal";
import LoadingModal from "../components/organisms/LoadingModal";
import HomeNavBar from '../components/organisms/HomeNavBar';
import { usePopupState } from "../hooks/usePopupState";
import { useIngredients } from "../contexts/IngredientsContext";
import { Ingredient, StorageMethod } from "../types/EntityTypes";
import { recognizeReceipt } from "../services/ServiceApi";

const MyIngredientsPage: React.FC = () => {
  const optionsModal = usePopupState(null);
  const receiptUploadModal = usePopupState<{ selectedFile: File | null }>({ selectedFile: null });
  const addIngredientModal = usePopupState<{
    purchaseDate: string;
    matchedItems: string[];
  }>({
    purchaseDate: '',
    matchedItems: [],
  });
  const editModal = usePopupState<Ingredient | null>(null);
  const { ingredients, updateIngredient } = useIngredients();
  const [loading, setLoading] = React.useState<boolean>(false);

  const handleReceiptUploadConfirm = async () => {
    const { selectedFile } = receiptUploadModal.state;

    if (!selectedFile) {
      alert("영수증 이미지를 업로드하세요.");
      return;
    }

    setLoading(true);
    try {
      const response = await recognizeReceipt(selectedFile);
      console.log("영수증 인식 응답:", response);

      addIngredientModal.setState({
        purchaseDate: response.구매일자 || "알 수 없음",
        matchedItems: response.matchedItems || [],
      });

      receiptUploadModal.close();
      addIngredientModal.open();
    } catch (error) {
      console.error("영수증 인식 실패:", error);
      alert("영수증 인식에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  const handleDirectInput = () => {
    addIngredientModal.setState({ purchaseDate: '', matchedItems: [""] });
    addIngredientModal.open();
    optionsModal.close();
  };

  const storageMethods = ingredients
    .map((item) => item.storageMethod)
    .filter((method): method is StorageMethod => method !== undefined);

  return (
    <div className="container container-fluid px-0">
      <CommonHeader pageTitle="나의 식재료" />

      {/* 식재료 카드 리스트 */}
      <IngredientCardContainer
        onAddClick={optionsModal.open}
        onCardClick={(ingredient) => {
          editModal.setState(ingredient);
          editModal.open();
        }}
      />

      {/* 식재료 추가 방법 모달 */}
      {optionsModal.isOpen && (
        <IngredientOptionsModal
          isOpen={optionsModal.isOpen}
          onClose={optionsModal.close}
          openReceiptInputModal={receiptUploadModal.open}
          openManualInputModal={handleDirectInput}
        />
      )}

      {/* 영수증 업로드 모달 */}
      {receiptUploadModal.isOpen && (
        <ReceiptUploadModal
          onConfirm={handleReceiptUploadConfirm}
          onCancel={receiptUploadModal.close}
          selectedFile={receiptUploadModal.state.selectedFile}
          fileChangeHandler={(file) => receiptUploadModal.setState({ selectedFile: file })}
        />
      )}

      {/* 식재료 추가 모달 */}
      {addIngredientModal.isOpen && (
        <AddIngredientModal
          purchaseDate={addIngredientModal.state.purchaseDate}
          matchedItems={addIngredientModal.state.matchedItems}
          onClose={addIngredientModal.close}
        />
      )}

      {/* 수정 모달 */}
      {editModal.isOpen && editModal.state && (
        <EditIngredientModal
          row={editModal.state}
          storageMethods={storageMethods} // StorageMethod 데이터 전달
          onSave={(updatedIngredientRow) => {
            const updatedIngredient = {
              ...updatedIngredientRow,
              ingredientId: editModal.state!.ingredientId,
            };
            updateIngredient(updatedIngredient);
            editModal.close();
          }}
          onCancel={editModal.close}
        />

      )}

      {/* 로딩 모달 */}
      {loading && <LoadingModal />}
      <HomeNavBar />
    </div>
  );
};

export default MyIngredientsPage;
