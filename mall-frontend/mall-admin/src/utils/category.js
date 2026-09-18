/**
 * 分类树工具
 *
 * 后端返回的是嵌套的分类树（顶级分类带 children），而 el-select 只能吃平铺选项，
 * 所以这里统一拍平成带层级的列表，供商品列表筛选、商品编辑、数据分析三处复用。
 */

/**
 * 把分类树拍平成选项列表
 *
 * @param {Array} tree 分类树
 * @param {number} depth 当前层级，用于在选项里缩进显示
 * @returns {Array<{id: number, name: string, depth: number, isLeaf: boolean}>}
 */
export function flattenCategories(tree, depth = 0, result = []) {
  ;(tree || []).forEach((item) => {
    const children = item.children || []
    result.push({
      id: item.id,
      name: item.name,
      depth,
      isLeaf: children.length === 0
    })
    if (children.length) {
      flattenCategories(children, depth + 1, result)
    }
  })
  return result
}

/**
 * 选项的显示文案：子分类用全角空格缩进，让层级在平铺的下拉里也能看出来
 * （下拉里父分类也是可选项，选它表示"这个分组下的全部商品"）
 */
export function categoryLabel(option) {
  return '　'.repeat(option.depth) + option.name
}
