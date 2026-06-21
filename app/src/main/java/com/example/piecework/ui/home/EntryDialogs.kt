package com.example.piecework.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.piecework.data.ProductEntity
import com.example.piecework.data.WorkRecordItem

@Composable
fun PieceRecordDialog(
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAddProduct: (name: String, unitPriceCents: Long) -> Unit,
    onUpdateProduct: (productId: Long, name: String, unitPriceCents: Long) -> Unit,
    onSave: (productId: Long, quantity: Int, remark: String) -> Unit
) {
    PieceRecordEditorDialog(
        title = "记一笔",
        products = products,
        initialProductId = products.firstOrNull()?.id ?: 0L,
        initialQuantity = "",
        initialRemark = "",
        onDismiss = onDismiss,
        onAddProduct = onAddProduct,
        onUpdateProduct = onUpdateProduct,
        onSave = onSave
    )
}

@Composable
fun EditPieceRecordDialog(
    record: WorkRecordItem,
    products: List<ProductEntity>,
    onDismiss: () -> Unit,
    onAddProduct: (name: String, unitPriceCents: Long) -> Unit,
    onUpdateProduct: (productId: Long, name: String, unitPriceCents: Long) -> Unit,
    onSave: (productId: Long, quantity: Int, remark: String) -> Unit,
    onDelete: () -> Unit
) {
    PieceRecordEditorDialog(
        title = "编辑计件",
        products = products,
        initialProductId = record.productId ?: products.firstOrNull()?.id ?: 0L,
        initialQuantity = record.quantity.toString(),
        initialRemark = record.remark,
        onDismiss = onDismiss,
        onAddProduct = onAddProduct,
        onUpdateProduct = onUpdateProduct,
        onSave = onSave,
        onDelete = onDelete
    )
}

@Composable
private fun PieceRecordEditorDialog(
    title: String,
    products: List<ProductEntity>,
    initialProductId: Long,
    initialQuantity: String,
    initialRemark: String,
    onDismiss: () -> Unit,
    onAddProduct: (name: String, unitPriceCents: Long) -> Unit,
    onUpdateProduct: (productId: Long, name: String, unitPriceCents: Long) -> Unit,
    onSave: (productId: Long, quantity: Int, remark: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var selectedProductId by remember(initialProductId) { mutableLongStateOf(initialProductId) }
    var expanded by remember { mutableStateOf(false) }
    var quantityText by remember(initialQuantity) { mutableStateOf(initialQuantity) }
    var remark by remember(initialRemark) { mutableStateOf(initialRemark) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var pendingProductSelection by remember { mutableStateOf<Pair<String, Long>?>(null) }

    LaunchedEffect(products, pendingProductSelection) {
        val pending = pendingProductSelection
        val addedProduct = pending?.let { (name, unitPriceCents) ->
            products.lastOrNull { it.name == name && it.unitPriceCents == unitPriceCents }
        }
        if (addedProduct != null) {
            selectedProductId = addedProduct.id
            pendingProductSelection = null
        } else if (selectedProductId == 0L && products.isNotEmpty()) {
            selectedProductId = products.first().id
        } else if (selectedProductId != 0L && products.none { it.id == selectedProductId } && products.isNotEmpty()) {
            selectedProductId = products.first().id
        }
    }

    val selectedProduct = products.firstOrNull { it.id == selectedProductId }
    val quantity = quantityText.toIntOrNull()
    val canSave = selectedProduct != null && quantity != null && quantity > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                selectedProduct?.let {
                                    "${it.name}  单价:${it.unitPriceCents.toYuanText()}"
                                } ?: "请新增产品",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("+ 新增产品") },
                                onClick = {
                                    expanded = false
                                    showAddProductDialog = true
                                }
                            )
                            products.forEach { product ->
                                DropdownMenuItem(
                                    text = { Text("${product.name}  单价:${product.unitPriceCents.toYuanText()}") },
                                    onClick = {
                                        selectedProductId = product.id
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { showAddProductDialog = true }
                    ) {
                        Text("新增")
                    }

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        enabled = selectedProduct != null,
                        onClick = { showProductDialog = true }
                    ) {
                        Text("编辑")
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter(Char::isDigit) },
                    label = { Text("数量") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val product = selectedProduct ?: return@TextButton
                    val safeQuantity = quantity ?: return@TextButton
                    onSave(product.id, safeQuantity, remark)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Text("删除")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )

    selectedProduct?.let { product ->
        if (showProductDialog) {
            ProductEditDialog(
                product = product,
                onDismiss = { showProductDialog = false },
                onSave = { name, unitPriceCents ->
                    onUpdateProduct(product.id, name, unitPriceCents)
                    showProductDialog = false
                }
            )
        }
    }

    if (showAddProductDialog) {
        ProductAddDialog(
            onDismiss = { showAddProductDialog = false },
            onSave = { name, unitPriceCents ->
                onAddProduct(name, unitPriceCents)
                pendingProductSelection = name to unitPriceCents
                showAddProductDialog = false
            }
        )
    }
}

@Composable
private fun ProductAddDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, unitPriceCents: Long) -> Unit
) {
    ProductFormDialog(
        title = "新增产品",
        initialName = "",
        initialPriceText = "",
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
private fun ProductEditDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onSave: (name: String, unitPriceCents: Long) -> Unit
) {
    ProductFormDialog(
        title = "编辑产品",
        initialName = product.name,
        initialPriceText = product.unitPriceCents.toYuanText(),
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
private fun ProductFormDialog(
    title: String,
    initialName: String,
    initialPriceText: String,
    onDismiss: () -> Unit,
    onSave: (name: String, unitPriceCents: Long) -> Unit
) {
    var nameText by remember(initialName) { mutableStateOf(initialName) }
    var priceText by remember(initialPriceText) { mutableStateOf(initialPriceText) }

    val priceCents = priceText.toCentsOrNull()
    val canSave = nameText.trim().isNotBlank() && priceCents != null && priceCents > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("产品名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("单价") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val safePrice = priceCents ?: return@TextButton
                    onSave(nameText.trim(), safePrice)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun AmountRecordDialog(
    title: String,
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, remark: String) -> Unit
) {
    AmountRecordEditorDialog(
        title = title,
        initialAmountText = "",
        initialRemark = "",
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@Composable
fun EditAmountRecordDialog(
    title: String,
    record: WorkRecordItem,
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, remark: String) -> Unit,
    onDelete: () -> Unit
) {
    val amount = if (record.subsidyCents > 0) record.subsidyCents else record.deductionCents

    AmountRecordEditorDialog(
        title = title,
        initialAmountText = amount.toYuanText(),
        initialRemark = record.remark,
        onDismiss = onDismiss,
        onSave = onSave,
        onDelete = onDelete
    )
}

@Composable
private fun AmountRecordEditorDialog(
    title: String,
    initialAmountText: String,
    initialRemark: String,
    onDismiss: () -> Unit,
    onSave: (amountCents: Long, remark: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var amountText by remember(initialAmountText) { mutableStateOf(initialAmountText) }
    var remark by remember(initialRemark) { mutableStateOf(initialRemark) }

    val amountCents = amountText.toCentsOrNull()
    val canSave = amountCents != null && amountCents > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("金额") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    val safeAmount = amountCents ?: return@TextButton
                    onSave(safeAmount, remark)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            Row {
                onDelete?.let {
                    TextButton(onClick = it) {
                        Text("删除")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        }
    )
}
