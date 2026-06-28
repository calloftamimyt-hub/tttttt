// supabase.js
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = process.env.SUPABASE_URL || 'https://acnlhvuqxstnocdbyugc.supabase.co'
const supabaseKey = process.env.SUPABASE_KEY || 'sb_publishable_g7LuLCfzyMkTdbyYQAYrVw_UOume_6B'

export const supabase = createClient(supabaseUrl, supabaseKey)
