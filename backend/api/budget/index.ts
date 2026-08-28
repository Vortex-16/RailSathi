import { query } from '../../src/db';
import { successResponse, errorResponse } from '../../src/utils/response';

const memoryBudgets = new Map<string, any>();

export default async function handler(req: any, res: any) {
  const userId = req.headers['x-user-id'] || req.query.userId || req.body?.userId;

  if (!userId) {
    return res.status(401).json(errorResponse('Unauthorized: User ID required', 'UNAUTHORIZED'));
  }

  const uId = String(userId);
  const currentMonth = new Date().toISOString().substring(0, 7); // e.g. "2026-08"

  if (req.method === 'GET') {
    // Read Budget
    try {
      const resDb = await query(
        `SELECT id, user_id, month_year, monthly_limit_inr, spent_inr
         FROM budgets
         WHERE user_id = $1 AND month_year = $2`,
        [uId, currentMonth]
      );

      if (resDb && resDb.rows && resDb.rows.length > 0) {
        const row = resDb.rows[0];
        const monthlyLimit = Number(row.monthly_limit_inr);
        const spent = Number(row.spent_inr);
        return res.status(200).json(successResponse({
          userId: uId,
          monthYear: currentMonth,
          monthlyLimit,
          spent,
          remaining: Math.max(0, monthlyLimit - spent)
        }));
      }

      const mem = memoryBudgets.get(`${uId}_${currentMonth}`);
      if (mem) {
        return res.status(200).json(successResponse(mem));
      }

      return res.status(200).json(successResponse({
        userId: uId,
        monthYear: currentMonth,
        monthlyLimit: 1500.0,
        spent: 0.0,
        remaining: 1500.0
      }));
    } catch (err: any) {
      return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
    }
  } else if (req.method === 'POST' || req.method === 'PUT') {
    // Create / Update Monthly Budget
    const { monthlyLimit, spent } = req.body || {};
    const limitNum = monthlyLimit != null ? Number(monthlyLimit) : 1500.0;
    const spentNum = spent != null ? Number(spent) : 0.0;

    try {
      await query(
        `INSERT INTO budgets (user_id, month_year, monthly_limit_inr, spent_inr)
         VALUES ($1, $2, $3, $4)
         ON CONFLICT (user_id, month_year) DO UPDATE
         SET monthly_limit_inr = EXCLUDED.monthly_limit_inr,
             spent_inr = COALESCE($4, budgets.spent_inr),
             updated_at = CURRENT_TIMESTAMP`,
        [uId, currentMonth, limitNum, spentNum]
      );

      const budgetData = {
        userId: uId,
        monthYear: currentMonth,
        monthlyLimit: limitNum,
        spent: spentNum,
        remaining: Math.max(0, limitNum - spentNum)
      };

      memoryBudgets.set(`${uId}_${currentMonth}`, budgetData);
      return res.status(200).json(successResponse(budgetData));
    } catch (err: any) {
      return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
    }
  } else if (req.method === 'DELETE') {
    // Reset / Delete Budget
    try {
      await query(
        `DELETE FROM budgets WHERE user_id = $1 AND month_year = $2`,
        [uId, currentMonth]
      );
      memoryBudgets.delete(`${uId}_${currentMonth}`);
      return res.status(200).json(successResponse({ reset: true, userId: uId, monthYear: currentMonth }));
    } catch (err: any) {
      return res.status(500).json(errorResponse(err.message || 'Internal Server Error'));
    }
  } else {
    return res.status(405).json(errorResponse('Method Not Allowed', 'METHOD_NOT_ALLOWED'));
  }
}
